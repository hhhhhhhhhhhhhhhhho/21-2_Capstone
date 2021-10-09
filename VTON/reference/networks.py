#coding=utf-8
import torch
import torch.nn as nn
from torch.nn import init
from torchvision import models
import os

import numpy as np

from spectral_normalization import SpectralNorm

def weights_init_normal(m):
    classname = m.__class__.__name__
    if classname.find('Conv') != -1:
        init.normal_(m.weight.data, 0.0, 0.02)
    elif classname.find('Linear') != -1:
        init.normal(m.weight.data, 0.0, 0.02)
    elif classname.find('BatchNorm2d') != -1:
        init.normal_(m.weight.data, 1.0, 0.02)
        init.constant_(m.bias.data, 0.0)


def weights_init_xavier(m):
    classname = m.__class__.__name__
    if classname.find('Conv') != -1:
        init.xavier_normal_(m.weight.data, gain=0.02)
    elif classname.find('Linear') != -1:
        init.xavier_normal_(m.weight.data, gain=0.02)
    elif classname.find('BatchNorm2d') != -1:
        init.normal_(m.weight.data, 1.0, 0.02)
        init.constant_(m.bias.data, 0.0)


def weights_init_kaiming(m):
    classname = m.__class__.__name__
    if classname.find('Conv') != -1:
        init.kaiming_normal_(m.weight.data, a=0, mode='fan_in')
    elif classname.find('Linear') != -1:
        init.kaiming_normal_(m.weight.data, a=0, mode='fan_in')
    elif classname.find('BatchNorm2d') != -1:
        init.normal_(m.weight.data, 1.0, 0.02)
        init.constant_(m.bias.data, 0.0)


def init_weights(net, init_type='normal'):
    print('initialization method [%s]' % init_type)
    if init_type == 'normal':
        net.apply(weights_init_normal)
    elif init_type == 'xavier':
        net.apply(weights_init_xavier)
    elif init_type == 'kaiming':
        net.apply(weights_init_kaiming)
    else:
        raise NotImplementedError('initialization method [%s] is not implemented' % init_type)

class FeatureExtraction(nn.Module):
    def __init__(self, input_nc, ngf=64, n_layers=3, norm_layer=nn.BatchNorm2d, use_dropout=False):
        super(FeatureExtraction, self).__init__()
        downconv = nn.Conv2d(input_nc, ngf, kernel_size=4, stride=2, padding=1)
        model = [downconv, nn.ReLU(True), norm_layer(ngf)]
        for i in range(n_layers):
            in_ngf = 2**i * ngf if 2**i * ngf < 512 else 512
            out_ngf = 2**(i+1) * ngf if 2**i * ngf < 512 else 512
            downconv = nn.Conv2d(in_ngf, out_ngf, kernel_size=4, stride=2, padding=1)
            model += [downconv, nn.ReLU(True)]
            model += [norm_layer(out_ngf)]
        model += [nn.Conv2d(512, 512, kernel_size=3, stride=1, padding=1), nn.ReLU(True)]
        model += [norm_layer(512)]
        model += [nn.Conv2d(512, 512, kernel_size=3, stride=1, padding=1), nn.ReLU(True)]
        
        self.model = nn.Sequential(*model)
        init_weights(self.model, init_type='normal')

    def forward(self, x):
        return self.model(x)

class FeatureL2Norm(torch.nn.Module):
    def __init__(self):
        super(FeatureL2Norm, self).__init__()

    def forward(self, feature):
        epsilon = 1e-6
        norm = torch.pow(torch.sum(torch.pow(feature,2),1)+epsilon,0.5).unsqueeze(1).expand_as(feature)
        return torch.div(feature,norm)
    
class FeatureCorrelation(nn.Module):
    def __init__(self):
        super(FeatureCorrelation, self).__init__()
    
    def forward(self, feature_A, feature_B):
        b,c,h,w = feature_A.size()
        #print(feature_A.size())
        # reshape features for matrix multiplication
        feature_A = feature_A.transpose(2,3).contiguous().reshape(b,c,h*w)
        feature_B = feature_B.reshape(b,c,h*w).transpose(1,2)
        # perform matrix mult.
        feature_mul = torch.bmm(feature_B,feature_A)
        correlation_tensor = feature_mul.reshape(b,h,w,h*w).transpose(2,3).transpose(1,2)
        #print('corr tensor:',correlation_tensor.size())
        return correlation_tensor
    
class FeatureRegression(nn.Module):
    def __init__(self, input_nc=512,output_dim=6, use_cuda=True):
        super(FeatureRegression, self).__init__()
        self.conv = nn.Sequential(
            nn.Conv2d(input_nc, 512, kernel_size=4, stride=2, padding=1),
            nn.BatchNorm2d(512),
            nn.ReLU(inplace=True),
            nn.Conv2d(512, 256, kernel_size=4, stride=2, padding=1),
            nn.BatchNorm2d(256),
            nn.ReLU(inplace=True),
            nn.Conv2d(256, 128, kernel_size=3, padding=1),
            nn.BatchNorm2d(128),
            nn.ReLU(inplace=True),
            nn.Conv2d(128, 64, kernel_size=3, padding=1),
            nn.BatchNorm2d(64),
            nn.ReLU(inplace=True),
        )
        self.linear = nn.Linear(64 * 8 * 6, output_dim) #512 384
        #self.linear = nn.Linear(64 * 4 * 3, output_dim) #256 192
        #self.linear = nn.Linear(640, output_dim) # 320 180
        #self.linear = nn.Linear(64 * 6 * 4, output_dim) #384 288
        self.tanh = nn.Tanh()
        if use_cuda:
            self.conv.cuda()
            self.linear.cuda()
            self.tanh.cuda()

    def forward(self, x):
        x = self.conv(x)
        x = x.reshape(x.size(0), -1)
        #print('xsize:',x.size())
        x = self.linear(x)
        x = self.tanh(x)
        return x

class AffineGridGen(nn.Module):
    #def __init__(self, out_h=256, out_w=192, out_ch = 3):
    def __init__(self, out_h=384, out_w=288, out_ch = 3):
        super(AffineGridGen, self).__init__()        
        self.out_h = out_h
        self.out_w = out_w
        self.out_ch = out_ch
        
    def forward(self, theta):
        theta = theta.contiguous()
        batch_size = theta.size()[0]
        out_size = torch.Size((batch_size,self.out_ch,self.out_h,self.out_w))
        return F.affine_grid(theta, out_size)
        
class TpsGridGen(nn.Module):
    #def __init__(self, out_h=256, out_w=192, use_regular_grid=True, grid_size=3, reg_factor=0, use_cuda=True):
    def __init__(self, out_h=384, out_w=288, use_regular_grid=True, grid_size=3, reg_factor=0, use_cuda=True):
        super(TpsGridGen, self).__init__()
        self.out_h, self.out_w = out_h, out_w
        self.reg_factor = reg_factor
        self.use_cuda = use_cuda

        # create grid in numpy
        self.grid = np.zeros( [self.out_h, self.out_w, 3], dtype=np.float32)
        # sampling grid with dim-0 coords (Y)
        self.grid_X,self.grid_Y = np.meshgrid(np.linspace(-1,1,out_w),np.linspace(-1,1,out_h))
        # grid_X,grid_Y: size [1,H,W,1,1]
        self.grid_X = torch.FloatTensor(self.grid_X).unsqueeze(0).unsqueeze(3)
        self.grid_Y = torch.FloatTensor(self.grid_Y).unsqueeze(0).unsqueeze(3)
        if use_cuda:
            self.grid_X = self.grid_X.cuda()
            self.grid_Y = self.grid_Y.cuda()

        # initialize regular grid for control points P_i
        if use_regular_grid:
            axis_coords = np.linspace(-1,1,grid_size)
            self.N = grid_size*grid_size
            P_Y,P_X = np.meshgrid(axis_coords,axis_coords)
            P_X = np.reshape(P_X,(-1,1)) # size (N,1)
            P_Y = np.reshape(P_Y,(-1,1)) # size (N,1)
            P_X = torch.FloatTensor(P_X)
            P_Y = torch.FloatTensor(P_Y)
            self.P_X_base = P_X.clone()
            self.P_Y_base = P_Y.clone()
            self.Li = self.compute_L_inverse(P_X,P_Y).unsqueeze(0)
            self.P_X = P_X.unsqueeze(2).unsqueeze(3).unsqueeze(4).transpose(0,4)
            self.P_Y = P_Y.unsqueeze(2).unsqueeze(3).unsqueeze(4).transpose(0,4)
            if use_cuda:
                self.P_X = self.P_X.cuda()
                self.P_Y = self.P_Y.cuda()
                self.P_X_base = self.P_X_base.cuda()
                self.P_Y_base = self.P_Y_base.cuda()

            
    def forward(self, theta):
        warped_grid = self.apply_transformation(theta,torch.cat((self.grid_X,self.grid_Y),3))
        
        return warped_grid
    
    def compute_L_inverse(self,X,Y):
        N = X.size()[0] # num of points (along dim 0)
        # construct matrix K
        Xmat = X.expand(N,N)
        Ymat = Y.expand(N,N)
        P_dist_squared = torch.pow(Xmat-Xmat.transpose(0,1),2)+torch.pow(Ymat-Ymat.transpose(0,1),2)
        P_dist_squared[P_dist_squared==0]=1 # make diagonal 1 to avoid NaN in log computation
        K = torch.mul(P_dist_squared,torch.log(P_dist_squared))
        # construct matrix L
        O = torch.FloatTensor(N,1).fill_(1)
        Z = torch.FloatTensor(3,3).fill_(0)       
        P = torch.cat((O,X,Y),1)
        L = torch.cat((torch.cat((K,P),1),torch.cat((P.transpose(0,1),Z),1)),0)
        Li = torch.inverse(L)
        if self.use_cuda:
            Li = Li.cuda()
        return Li
        
    def apply_transformation(self,theta,points):
        if theta.dim()==2:
            theta = theta.unsqueeze(2).unsqueeze(3)
        # points should be in the [B,H,W,2] format,
        # where points[:,:,:,0] are the X coords  
        # and points[:,:,:,1] are the Y coords  
        
        # input are the corresponding control points P_i
        batch_size = theta.size()[0]
        # split theta into point coordinates
        Q_X=theta[:,:self.N,:,:].squeeze(3)
        Q_Y=theta[:,self.N:,:,:].squeeze(3)
        Q_X = Q_X + self.P_X_base.expand_as(Q_X)
        Q_Y = Q_Y + self.P_Y_base.expand_as(Q_Y)
        
        # get spatial dimensions of points
        points_b = points.size()[0]
        points_h = points.size()[1]
        points_w = points.size()[2]
        
        # repeat pre-defined control points along spatial dimensions of points to be transformed
        P_X = self.P_X.expand((1,points_h,points_w,1,self.N))
        P_Y = self.P_Y.expand((1,points_h,points_w,1,self.N))
        
        # compute weigths for non-linear part
        W_X = torch.bmm(self.Li[:,:self.N,:self.N].expand((batch_size,self.N,self.N)),Q_X)
        W_Y = torch.bmm(self.Li[:,:self.N,:self.N].expand((batch_size,self.N,self.N)),Q_Y)
        # reshape
        # W_X,W,Y: size [B,H,W,1,N]
        W_X = W_X.unsqueeze(3).unsqueeze(4).transpose(1,4).repeat(1,points_h,points_w,1,1)
        W_Y = W_Y.unsqueeze(3).unsqueeze(4).transpose(1,4).repeat(1,points_h,points_w,1,1)
        # compute weights for affine part
        A_X = torch.bmm(self.Li[:,self.N:,:self.N].expand((batch_size,3,self.N)),Q_X)
        A_Y = torch.bmm(self.Li[:,self.N:,:self.N].expand((batch_size,3,self.N)),Q_Y)
        # reshape
        # A_X,A,Y: size [B,H,W,1,3]
        A_X = A_X.unsqueeze(3).unsqueeze(4).transpose(1,4).repeat(1,points_h,points_w,1,1)
        A_Y = A_Y.unsqueeze(3).unsqueeze(4).transpose(1,4).repeat(1,points_h,points_w,1,1)
        
        # compute distance P_i - (grid_X,grid_Y)
        # grid is expanded in point dim 4, but not in batch dim 0, as points P_X,P_Y are fixed for all batch
        points_X_for_summation = points[:,:,:,0].unsqueeze(3).unsqueeze(4).expand(points[:,:,:,0].size()+(1,self.N))
        points_Y_for_summation = points[:,:,:,1].unsqueeze(3).unsqueeze(4).expand(points[:,:,:,1].size()+(1,self.N))
        
        if points_b==1:
            delta_X = points_X_for_summation-P_X
            delta_Y = points_Y_for_summation-P_Y
        else:
            # use expanded P_X,P_Y in batch dimension
            delta_X = points_X_for_summation-P_X.expand_as(points_X_for_summation)
            delta_Y = points_Y_for_summation-P_Y.expand_as(points_Y_for_summation)
            
        dist_squared = torch.pow(delta_X,2)+torch.pow(delta_Y,2)
        # U: size [1,H,W,1,N]
        dist_squared[dist_squared==0]=1 # avoid NaN in log computation
        U = torch.mul(dist_squared,torch.log(dist_squared)) 
        
        # expand grid in batch dimension if necessary
        points_X_batch = points[:,:,:,0].unsqueeze(3)
        points_Y_batch = points[:,:,:,1].unsqueeze(3)
        if points_b==1:
            points_X_batch = points_X_batch.expand((batch_size,)+points_X_batch.size()[1:])
            points_Y_batch = points_Y_batch.expand((batch_size,)+points_Y_batch.size()[1:])
        
        points_X_prime = A_X[:,:,:,:,0]+ \
                       torch.mul(A_X[:,:,:,:,1],points_X_batch) + \
                       torch.mul(A_X[:,:,:,:,2],points_Y_batch) + \
                       torch.sum(torch.mul(W_X,U.expand_as(W_X)),4)
                    
        points_Y_prime = A_Y[:,:,:,:,0]+ \
                       torch.mul(A_Y[:,:,:,:,1],points_X_batch) + \
                       torch.mul(A_Y[:,:,:,:,2],points_Y_batch) + \
                       torch.sum(torch.mul(W_Y,U.expand_as(W_Y)),4)
        
        return torch.cat((points_X_prime,points_Y_prime),3)
        
# Defines the Unet generator.
# |num_downs|: number of downsamplings in UNet. For example,
# if |num_downs| == 7, image of size 128x128 will become of size 1x1
# at the bottleneck
class UnetGenerator(nn.Module):
    def __init__(self, input_nc, output_nc, num_downs, ngf=64,
                 norm_layer=nn.BatchNorm2d, use_dropout=False):
        ##<< [ SC ]
        #input_nc = 25, output_nc = 4, num_downs=6, ngf=64

        super(UnetGenerator, self).__init__()
        # construct unet structure
        unet_block = UnetSkipConnectionBlock(ngf * 8, ngf * 8, input_nc=None, submodule=None, norm_layer=norm_layer, innermost=True)
        for i in range(num_downs - 5):
            unet_block = UnetSkipConnectionBlock(ngf * 8, ngf * 8, input_nc=None, submodule=unet_block, norm_layer=norm_layer, use_dropout=use_dropout)
        unet_block = UnetSkipConnectionBlock(ngf * 4, ngf * 8, input_nc=None, submodule=unet_block, norm_layer=norm_layer)
        unet_block = UnetSkipConnectionBlock(ngf * 2, ngf * 4, input_nc=None, submodule=unet_block, norm_layer=norm_layer)
        unet_block = UnetSkipConnectionBlock(ngf, ngf * 2, input_nc=None, submodule=unet_block, norm_layer=norm_layer)
        unet_block = UnetSkipConnectionBlock(output_nc, ngf, input_nc=input_nc, submodule=unet_block, outermost=True, norm_layer=norm_layer)

        self.model = unet_block

    def forward(self, input):
        #print('input:',input.size())
        return self.model(input)


# Defines the submodule with skip connection.
# X -------------------identity---------------------- X
#   |-- downsampling -- |submodule| -- upsampling --|
class UnetSkipConnectionBlock(nn.Module):
    def __init__(self, outer_nc, inner_nc, input_nc=None,
                 submodule=None, outermost=False, innermost=False, norm_layer=nn.BatchNorm2d, use_dropout=False):
        super(UnetSkipConnectionBlock, self).__init__()
        self.outermost = outermost
        use_bias = norm_layer == nn.InstanceNorm2d

        if input_nc is None:
            input_nc = outer_nc
        downconv = nn.Conv2d(input_nc, inner_nc, kernel_size=4,
                             stride=2, padding=1, bias=use_bias)
        downrelu = nn.LeakyReLU(0.2, True)
        downnorm = norm_layer(inner_nc)
        uprelu = nn.ReLU(True)
        upnorm = norm_layer(outer_nc)

        if outermost:
            upsample = nn.Upsample(scale_factor=2, mode='bilinear')
            upconv = nn.Conv2d(inner_nc * 2, outer_nc, kernel_size=3, stride=1, padding=1, bias=use_bias)
            down = [downconv]
            up = [uprelu, upsample, upconv, upnorm]
            model = down + [submodule] + up
        elif innermost:
            upsample = nn.Upsample(scale_factor=2, mode='bilinear')
            upconv = nn.Conv2d(inner_nc, outer_nc, kernel_size=3, stride=1, padding=1, bias=use_bias)
            down = [downrelu, downconv]
            up = [uprelu, upsample, upconv, upnorm]
            model = down + up
        else:
            upsample = nn.Upsample(scale_factor=2, mode='bilinear')
            upconv = nn.Conv2d(inner_nc*2, outer_nc, kernel_size=3, stride=1, padding=1, bias=use_bias)
            down = [downrelu, downconv, downnorm]

            up = [uprelu, upsample, upconv, upnorm]

            if use_dropout:
                model = down + [submodule] + up + [nn.Dropout(0.5)]
            else:
                model = down + [submodule] + up

        self.model = nn.Sequential(*model)

    def forward(self, x):
        if self.outermost:
            return self.model(x)
        else:
            #print(self.model)
            #print('x:',x.size(), 'model',self.model(x).size())
            return torch.cat([x, self.model(x)], 1)

class Discriminator(nn.Module):
    def __init__(self, ngf=64):
        super(Discriminator, self).__init__()
        self.ngf = ngf
        self.n_features=[3, 32, 64, 128, 256, 512, 1]


        self.layer1 = self.AddConvBlock_for_Discriminator(self.n_features[0], self.n_features[1], use_batch=False)
        self.layer2 = self.AddConvBlock_for_Discriminator(self.n_features[1], self.n_features[2], use_batch=True)
        self.layer3 = self.AddConvBlock_for_Discriminator(self.n_features[2], self.n_features[3], use_batch=True)
        self.layer4 = self.AddConvBlock_for_Discriminator(self.n_features[3], self.n_features[4], use_batch=True)
        self.layer5 = self.AddConvBlock_for_Discriminator(self.n_features[4], self.n_features[5], use_batch=True)
        self.layer6 = nn.Conv2d(self.n_features[len(self.n_features)-2], self.n_features[len(self.n_features)-1],kernel_size=(8,6),stride=1,padding=0)
        self.activation = nn.Sigmoid()
        #self.layer5 = self.AddConvBlock_for_Discriminator(self.n_features[4], self.n_features[5], use_batch=True)

        #layer_list=[]
        #for i in range(0,len(self.n_features)-1):
        #    if i==0:
        #        using_batchnorm = False
        #    else:
        #        using_batchnorm = True
        #    layer_list.append(self.AddConvBlock_for_Discriminator(self.n_features[i], self.n_features[i+1], use_batch=using_batchnorm))

        #print("DISCRIMINATOR")
        #layer_list.append(nn.Conv2d(self.n_features[len(self.n_features)-2], self.n_features[len(self.n_features)-1],kernel_size=(4,4),stride=1,padding=0))
        #layer_list.append(self.activation)
        #self.model = nn.Sequential(*layer_list)
        #print(self.layer_list)

    def AddConvBlock_for_Discriminator(self, input_nc, output_nc, kernel_size=(4,4), use_batch=True):
        if use_batch:
            submodel = nn.Sequential(
                nn.Conv2d(input_nc, output_nc, kernel_size=kernel_size, stride=2, padding=1),
                nn.BatchNorm2d(output_nc),
                nn.LeakyReLU(0.2, True)
            )
        else:
            submodel = nn.Sequential(
                nn.Conv2d(input_nc, output_nc, kernel_size=kernel_size, stride=2, padding=1),
                nn.LeakyReLU(0.2, True)
            )
        return submodel

    def forward(self, x):
        #print('before layer1:', x.size())
        x = self.layer1(x)
        #print('before layer2:', x.size())
        x = self.layer2(x)
        #print('before layer3:', x.size())
        x = self.layer3(x)
        #print('before layer4:', x.size())
        x = self.layer4(x)
        #print('before layer5:', x.size())
        x = self.layer5(x)
        #print('after layer5:',x.size())
        x = self.layer6(x)
        #print('before activation:', x.size())

        x = self.activation(x)
        #for i in range(0,len(self.layer_list)):
        #    print('input x size:', x.size())
        #    x = self.layer_list[i](x)
        #    print('output x size:', x.size())
        return x[:,0,0]

class SNPatchDiscriminator(nn.Module):
    def __init__(self, ngf=64):
        super(SNPatchDiscriminator, self).__init__()
        self.ngf = ngf
        self.n_features=[3, ngf, ngf*2, ngf*4, ngf*4]

        self.layer1 = self.AddConvBlock_for_Discriminator(self.n_features[0], self.n_features[1], use_batch=False) # 512 384 -> 256 192
        self.layer2 = self.AddConvBlock_for_Discriminator(self.n_features[1], self.n_features[2], use_batch=True) # 256, 192 -> 128, 96
        self.layer3 = self.AddConvBlock_for_Discriminator(self.n_features[2], self.n_features[3], use_batch=True) # 128, 96 -> 64, 48
        self.layer4 = self.AddConvBlock_for_Discriminator(self.n_features[3], self.n_features[4], use_batch=True) # 64, 48 -> 32, 24
        self.layer5 = SpectralNorm(nn.Conv2d(self.n_features[4],self.n_features[4], kernel_size= (5,5), stride=1, padding=2)) # 32, 24 -> 32, 24
        self.bn5 = nn.BatchNorm2d(self.n_features[4])
        self.LReLU5 = nn.LeakyReLU(0.2, True)
        self.layer6 = SpectralNorm(nn.Conv2d(self.n_features[4], 1, kernel_size=1, stride=1, padding=0)) #32, 24
        #self.bn6 = nn.BatchNorm2d(1)
        #self.LReLU6 = nn.LeakyReLU(0.2, True)
        self.activation = nn.Sigmoid()
        #self.avgpooling = nn.AvgPool2d(kernel_size=(16,12))
        self.avgpooling = nn.AvgPool2d(kernel_size=(32,24)) # for 512x384
        #self.DownSampler = nn.Upsample(scale_factor=0.0625)



    def AddConvBlock_for_Discriminator(self, input_nc, output_nc, kernel_size=(4, 4), use_batch=True):
        if use_batch:
            submodel = nn.Sequential(
                    SpectralNorm(nn.Conv2d(input_nc, output_nc, kernel_size=kernel_size, stride=2, padding=1)),
                    nn.BatchNorm2d(output_nc),
                    nn.LeakyReLU(0.2, True)
                )
        else:
            submodel = nn.Sequential(
                SpectralNorm(nn.Conv2d(input_nc, output_nc, kernel_size=kernel_size, stride=2, padding=1)),
                nn.LeakyReLU(0.2, True)
            )
        return submodel

    def forward(self,x):
        #print('before layer1:', x.size())
        x = self.layer1(x)
        #print('before layer2:', x.size())
        x = self.layer2(x)
        #print('before layer3:', x.size())
        x = self.layer3(x)
        #print('before layer4:', x.size())
        x = self.layer4(x)
        #print('before layer5:', x.size())
        x = self.layer5(x)
        x = self.bn5(x)
        x = self.LReLU5(x)
        #print('before layer6:', x.size())
        x = self.layer6(x)
        #x = self.bn6(x)
        #x = self.LReLU6(x)
        #print('before avg pool:', x.size())
        #print('before activation:', x.size())
        #binary_mask = self.DownSampler(binary_mask)
        #print('sizecomp:',x.size(),binary_mask.size())
        #x = torch.mul(x,binary_mask)
        x = self.activation(x)
        x = self.avgpooling(x)


        #print('output:',x.size())
        #for i in range(0,len(self.layer_list)):
        #    print('input x size:', x.size())
        #    x = self.layer_list[i](x)
        #    print('output x size:', x.size())
        return x[:,0,0]

##<< [ SC : unet refinement layer ]
class UnetRefinement(nn.Module):

    def __init__(self):
        super(UnetRefinement, self).__init__()
        self.refinementLayer = DilatedCNN()

    def forward(self, x):
        return self.refinementLayer(x)

class DilatedCNN(nn.Module):
    def __init__(self, input_nc=64, output_nc=3):
        super(DilatedCNN, self).__init__()
        self.downConv = nn.Sequential(
            nn.Conv2d(25, 64, kernel_size=5, stride=1, padding=1),
            nn.InstanceNorm2d(64),
            nn.Conv2d(64, 128, kernel_size=3, stride=2, padding=1),
            nn.LeakyReLU(0.2, True),
            nn.InstanceNorm2d(128),
            nn.Conv2d(128, 256, kernel_size=3, stride=1, padding=1),
            nn.ReLU(),
            nn.InstanceNorm2d(256),
            nn.Conv2d(256, 512, kernel_size=3, stride=2, padding=1),
            nn.ReLU(),
        )

        self.upConv = nn.Sequential(
            nn.ReLU(),
            nn.Conv2d(512, 256, kernel_size=3, stride=1, padding=1),
            nn.InstanceNorm2d(256),
            nn.ReLU(),
            nn.Upsample(scale_factor=2, mode='bilinear'),
            nn.Conv2d(256, 128, kernel_size=3, stride=1, padding=1),
            nn.InstanceNorm2d(128),
            nn.ReLU(),
            nn.Upsample(scale_factor=2, mode='bilinear'),
            nn.Conv2d(128, 64, kernel_size=3, stride=1, padding=1),
            nn.ReLU(),
            nn.Conv2d(64, output_nc, kernel_size=3, stride=1, padding=1),
        )
        dilated_nc = 512
        self.dilatedConv = nn.Sequential(
            nn.Conv2d(dilated_nc, dilated_nc, kernel_size=3,
                      #stride=1, padding=2, padding_mode='reflect', dilation=2),
                      stride=1, padding=2, dilation=2),
            nn.ReLU(),
            nn.Conv2d(in_channels=dilated_nc, out_channels=dilated_nc, kernel_size=3,
                      #stride=1, padding=4, padding_mode='reflect', dilation=4),
                      stride=1, padding=4, dilation=4),
            nn.ReLU(),
            # nn.Conv2d(in_channels=6, out_channels=3, kernel_size=3,
            #           stride=1, padding=1, dilation=1),
            # nn.ReLU(),
        )

    def forward(self, x):
        x = self.downConv(x)
        x = self.dilatedConv(x)
        return self.upConv(x)

class Vgg19(nn.Module):
    def __init__(self, requires_grad=False):
        super(Vgg19, self).__init__()
        vgg_pretrained_features = models.vgg19(pretrained=True).features
        self.slice1 = torch.nn.Sequential()
        self.slice2 = torch.nn.Sequential()
        self.slice3 = torch.nn.Sequential()
        self.slice4 = torch.nn.Sequential()
        self.slice5 = torch.nn.Sequential()
        for x in range(2):
            self.slice1.add_module(str(x), vgg_pretrained_features[x])
        for x in range(2, 7):
            self.slice2.add_module(str(x), vgg_pretrained_features[x])
        for x in range(7, 12):
            self.slice3.add_module(str(x), vgg_pretrained_features[x])
        for x in range(12, 21):
            self.slice4.add_module(str(x), vgg_pretrained_features[x])
        for x in range(21, 30):
            self.slice5.add_module(str(x), vgg_pretrained_features[x])
        if not requires_grad:
            for param in self.parameters():
                param.requires_grad = False

    def forward(self, X):
        h_relu1 = self.slice1(X)
        h_relu2 = self.slice2(h_relu1)
        h_relu3 = self.slice3(h_relu2)
        h_relu4 = self.slice4(h_relu3)
        h_relu5 = self.slice5(h_relu4)
        out = [h_relu1, h_relu2, h_relu3, h_relu4, h_relu5]
        return out

class VGGLoss(nn.Module):
    def __init__(self, layids = None):
        super(VGGLoss, self).__init__()
        self.vgg = Vgg19()
        self.vgg.cuda()
        self.criterion = nn.L1Loss()
        self.weights = [1.0/32, 1.0/16, 1.0/8, 1.0/4, 1.0]
        self.layids = layids

    def forward(self, x, y):
        x_vgg, y_vgg = self.vgg(x), self.vgg(y)
        loss = 0
        if self.layids is None:
            self.layids = list(range(len(x_vgg)))
        for i in self.layids:
            loss += self.weights[i] * self.criterion(x_vgg[i], y_vgg[i].detach())
        return loss

class GMM(nn.Module):
    """ Geometric Matching Module
    """
    def __init__(self, opt):
        super(GMM, self).__init__()
        #self.extractionA = FeatureExtraction(22, ngf=64, n_layers=3, norm_layer=nn.BatchNorm2d)
        self.extractionA = FeatureExtraction(23, ngf=64, n_layers=3, norm_layer=nn.BatchNorm2d)  
        self.extractionB = FeatureExtraction(3, ngf=64, n_layers=3, norm_layer=nn.BatchNorm2d)
        self.l2norm = FeatureL2Norm()
        self.correlation = FeatureCorrelation()
        self.regression = FeatureRegression(input_nc=192, output_dim=2*opt.grid_size**2, use_cuda=True)
        self.gridGen = TpsGridGen(opt.fine_height, opt.fine_width, use_cuda=True, grid_size=opt.grid_size)
        
    def forward(self, inputA, inputB):
        #print('inputA:',inputA.size())
        #print('inputB:',inputB.size())
        featureA = self.extractionA(inputA)
        featureB = self.extractionB(inputB)
        featureA = self.l2norm(featureA)
        featureB = self.l2norm(featureB)


        correlation = self.correlation(featureA, featureB)

        theta = self.regression(correlation)
        grid = self.gridGen(theta)
        return grid, theta

class GMM_ETRI(nn.Module):
    """ Geometric Matching Module
    """
    def __init__(self, opt):
        super(GMM_ETRI, self).__init__()
        #self.extractionA = FeatureExtraction(22, ngf=64, n_layers=3, norm_layer=nn.BatchNorm2d)
        self.extractionA = FeatureExtraction(23, ngf=64, n_layers=3, norm_layer=nn.BatchNorm2d)  
        self.extractionB = FeatureExtraction(3, ngf=64, n_layers=3, norm_layer=nn.BatchNorm2d)
        self.l2norm = FeatureL2Norm()
        self.correlation = FeatureCorrelation()
        #self.regression = FeatureRegression(input_nc=192, output_dim=2*opt.grid_size**2, use_cuda=True) #256 x192
        self.regression = FeatureRegression(input_nc=768, output_dim=2*opt.grid_size**2, use_cuda=True) #256 x192
        #self.regression = FeatureRegression(input_nc=220, output_dim=2*opt.grid_size**2, use_cuda=True)
        #self.regression = FeatureRegression(input_nc=432, output_dim=2*opt.grid_size**2, use_cuda=True)
        self.gridGen = TpsGridGen(opt.fine_height, opt.fine_width, use_cuda=True, grid_size=opt.grid_size)
        
    def forward(self, inputA, inputB):
        #print('input:', inputA.size(), inputB.size())
        featureA = self.extractionA(inputA)
        featureB = self.extractionB(inputB)
        featureA = self.l2norm(featureA)
        featureB = self.l2norm(featureB)
        #print('feature:', featureA.size(), featureB.size())
        #print('!:', featureA.size(), featureB.size())
        correlation = self.correlation(featureA, featureB)

        #print('correlation:', correlation.size())

        theta = self.regression(correlation)
        grid = self.gridGen(theta)
        return grid, theta

def save_checkpoint(model, save_path):
    if not os.path.exists(os.path.dirname(save_path)):
        os.makedirs(os.path.dirname(save_path))

    torch.save(model.cpu().state_dict(), save_path)
    model.cuda()

def load_checkpoint(model, checkpoint_path):
    if not os.path.exists(checkpoint_path):
        return
    model.load_state_dict(torch.load(checkpoint_path))
    model.cuda()

def save_coarse_refine_checkpoints(coarse, refine, save_path):
    if not os.path.exists(os.path.dirname(save_path)):
        os.makedirs(os.path.dirname(save_path))
    params = {}
    params['coarse'] = coarse.cpu().state_dict()
    params['refine'] = refine.cpu().state_dict()
    torch.save(params, save_path)
    coarse.cuda()
    refine.cuda()

