#coding=utf-8
import torch
import torch.utils.data as data
import torchvision.transforms as transforms

from PIL import Image
from PIL import ImageDraw

import os.path as osp
import numpy as np
import json
import copy

import cv2

class ETRIDataset(data.Dataset):
    """Dataset for 2021 AI DATA(65)."""

    def __init__(self, opt):
        super(ETRIDataset, self).__init__()
        # base setting
        self.opt = opt
        self.root = opt.dataroot
        self.datamode = opt.datamode # train or test or self-defined
        self.stage = opt.stage # GMM or TOM
        #self.data_list = opt.data_list
        self.fine_height = opt.fine_height
        self.fine_width = opt.fine_width
        self.radius = opt.radius
        self.data_path = osp.join(opt.dataroot, opt.datamode)
        self.transform = transforms.Compose([  \
                transforms.ToTensor(),   \
                # transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))])
            transforms.Normalize((0.5,), (0.5,))])

        self.front_or_back = opt.front_or_back
        self.ann_list = []

        with open(osp.join(opt.dataroot, self.datamode, 'wearing_info_' + self.datamode + '.json'), 'r') as f:
            self.ann_list = json.load(f)

    def name(self):
        return "ETRIDataset"

    def __getitem__(self, index):
        ann = self.ann_list[index]        

        s_file = ann['wearing'] #Model-Image file
        #print(ann)
        #w_info = [ ann['hat'], ann['top_out'], ann['top_in'], ann['bottom'] , ann['shoes'] ] #Wearing Information
        w_info = [ ann['hat'], ann['main_top'], ann['inner_top'], ann['bottom'] , ann['shoes'] ] #Wearing Information

        Model_Image_file = osp.join(self.root, self.datamode, 'Model-Image_deid', s_file)
        Model_Parse_file = osp.join(self.root, self.datamode, 'Model-Parse_p', s_file.replace('.jpg', '.png'))
        Item_Image_file =''
        Item_Parse_file =''
        if self.stage == 'GMM_ETRI' or self.stage == 'GMMBODY_ETRI':
            Item_Image_file = osp.join(self.root, self.datamode, 'Item-Image', ann['main_top']+'_F.jpg')
            Item_Parse_file = osp.join(self.root, self.datamode, 'Item-Parse_p', ann['main_top']+'_F.png')
        elif self.stage == 'TOM_SNPATCH_ETRI':
            Item_Image_file = osp.join(self.root, self.datamode, 'Item-Image'+self.opt.posfix_warp, ann['main_top'] + '_F' + '__' + s_file)
            Item_Parse_file = osp.join(self.root, self.datamode, 'Item-Parse_p'+self.opt.posfix_warp, ann['main_top'] + '_F' + '__' + s_file[:-3] + 'png')

        Keypoints_file = osp.join(self.root, self.datamode, 'Model-Pose_f', s_file[:-4] + '.json')

        if not osp.isfile(Model_Image_file):
            print('MODEL_IMAGE FILE: ', Model_Image_file, ' DOES NOT EXIST')
            exit(0)
        if not osp.isfile(Model_Parse_file):
            print('MODEL_PARSE FILE: ', Model_Parse_file, ' DOES NOT EXIST')
            exit(0)
        if not osp.isfile(Item_Image_file):
            print('ITEM_IMAGE FILE: ', Item_Image_file, ' DOES NOT EXIST')
            exit(0)
        if not osp.isfile(Item_Parse_file):
            print('ITEM_PARSE FILE: ', Item_Parse_file, ' DOES NOT EXIST')
            exit(0)
        if not osp.isfile(Keypoints_file):
            print('ITEM_PARSE FILE: ', Keypoints_file, ' DOES NOT EXIST')
            exit(0)

        c_ori = cv2.cvtColor(cv2.imread(Item_Image_file),cv2.COLOR_BGR2RGB)

        cm = cv2.cvtColor(cv2.imread(Item_Parse_file),cv2.COLOR_BGR2RGB)[:,:,0]
        cm = cm[:,:,None]

        c_parse = copy.deepcopy(cm).astype(np.float32)
        c_parse = c_parse.transpose(2,0,1)

        if self.stage=='GMMBODY_ETRI' and self.datamode == 'train':
            mask_c = (cm>0).astype(np.uint8)
            mask_hidden = (cm==6).astype(np.uint8)
            mask_sleevel = (cm==4).astype(np.uint8)
            mask_sleever = (cm==3).astype(np.uint8)
            mask_b = (cm==0).astype(np.uint8) + mask_hidden+mask_sleevel+mask_sleever
            mask_c = mask_c - mask_hidden - mask_sleevel -mask_sleever
        else:
            mask_c = (cm>0).astype(np.uint8)
            mask_hidden = (cm==6).astype(np.uint8)
            mask_b = (cm==0).astype(np.uint8) + mask_hidden
            mask_c -= mask_hidden

        if self.stage == 'GMM_ETRI' or self.stage=='GMMBODY_ETRI':
            cm = (cm / 4.0).astype(np.float32)
            #print('cori:',c_ori.shape, 'mask', mask_c.shape)
            tMean, tStd = cv2.meanStdDev(c_ori, mask=mask_c)
            #print(tMean.mean())
            mask_v = 255
            if tMean.mean()>200.0:
                mask_v = 0

            c = c_ori*mask_c  + (mask_b*mask_v)
            c_ori = c_ori*mask_c + (mask_b*255)

        elif self.stage == 'TOM_SNPATCH_ETRI':
            cm = (cm>0).astype(np.float32)
            c = c_ori
        
        c = self.transform(c)
        cm = cm.transpose(2,0,1)
        cm = torch.from_numpy(cm)


        c_ori = self.transform(c_ori)
        #c_ori = c_ori.transpose(2,0,1)
        #c_ori = torch.from_numpy(c_ori)
        
        # load parsing image
        #parse_name = s_file.replace('.jpg', '.png')
        
        #print(mparse_file)
        parse_array = cv2.cvtColor(cv2.imread(Model_Parse_file),cv2.COLOR_BGR2RGB)[:,:]
        #print('parse_array shape:',parse_array.shape)
        #parse_array = parse_array / 20.0
        parse_shape = (parse_array >0).astype(np.float32)

        parse_head = (parse_array ==2).astype(np.float32) + \
                     (parse_array==3).astype(np.float32) + \
                     (parse_array==4).astype(np.float32) #hair, face, neck

        if self.stage == 'GMMBODY_ETRI':
            parse_cloth = (parse_array==8).astype(np.float32) # only torso
            parse_occ = (parse_array==2).astype(np.float32) + \
                     (parse_array==3).astype(np.float32) + \
                     (parse_array==11).astype(np.float32) + \
                     (parse_array==12).astype(np.float32) + \
                     (parse_array==9).astype(np.float32) + \
                     (parse_array==10).astype(np.float32)
        else:
            parse_cloth = (parse_array==8).astype(np.float32) + \
                     (parse_array==9).astype(np.float32) + \
                     (parse_array==10).astype(np.float32) #inner torso, rsleeve, lsleeve
            parse_occ = (parse_array==2).astype(np.float32) + \
                     (parse_array==3).astype(np.float32) + \
                     (parse_array==11).astype(np.float32) + \
                     (parse_array==12).astype(np.float32) 

        parse_occ = 1-parse_occ
        parse_occ = parse_occ.transpose(2,0,1)
        parse_occ = torch.from_numpy(parse_occ)

        ## Remain only inner torso rsleeve lsleeve
        #parse_cm_mask = (parse_array==8).astype(np.float32) + \
        #         (parse_array==9).astype(np.float32) + \
        #         (parse_array==10).astype(np.float32) #inner torso, rsleeve, lsleeve
        #parse_cm = parse_array*parse_cm_mask

        #print('max:',np.max(parse_cm))
        #parse_cm = parse_cm[:,:,0]
        #parse_cm = parse_cm[:,:,None]
        #parse_cm[parse_cm>0] -= 7 # inner torso = 1, r = 2, l =3 // product torso=3 r=1 l=2
        #parse_cm[parse_cm==1] +=3
        #parse_cm[parse_cm>0] -=1

        #if self.stage == 'GMM_ETRI' or self.stage=='GMMBODY_ETRI':
        #    parse_cm = (parse_cm / 4.0).astype(np.float32)
        #elif self.stage == 'TOM_SNPATCH_ETRI':
        #    parse_cm = (parse_cm>0).astype(np.float32)

        #test_parse_cm = (parse_cm==3).astype(np.float32)
        #test_cm = (tt_cm==3).astype(np.float32)
        #cv2.imshow('test pcm', test_parse_cm)
        #cv2.imshow('cm', test_cm)
        #cv2.waitKey(0)

        #parse_cm = parse_cm.transpose(2,0,1)
        #parse_cm = torch.from_numpy(parse_cm)

        # shape downsample
        parse_shape = Image.fromarray((parse_shape*255).astype(np.uint8))
        parse_shape = parse_shape.resize((self.fine_width//16, self.fine_height//16), Image.BILINEAR)
        parse_shape = parse_shape.resize((self.fine_width, self.fine_height), Image.BILINEAR)
  
        #shape = self.transform_model(parse_shape) # [-1,1]
        shape = self.transform(parse_shape) # [-1,1]
        parse_head = parse_head.transpose(2,0,1)
        parse_cloth= parse_cloth.transpose(2,0,1)
        phead = torch.from_numpy(parse_head) # [0,1]
        pcm = torch.from_numpy(parse_cloth) # [0,1]

        # load person image
        im = cv2.cvtColor(cv2.imread(Model_Image_file),cv2.COLOR_BGR2RGB)
        mask_p = (parse_array>0).astype(np.uint8)
        mask_b = (parse_array==0).astype(np.uint8)
        #print(Model_Image_file)
        #print('sizecomp:', im.shape, mask_p.shape, mask_b.shape)
        if self.stage == 'GMM_ETRI' or self.stage=='GMMBODY_ETRI':
            im = im*mask_p + mask_b*mask_v

        im = self.transform(im) # [-1,1]
        #print(im)
        #exit(0)
        #im = self.transform_model(im)

        #print('im size:',im.size(), 'pcm',pcm.size(), 'phead', phead.size())
        # upper cloth
        if self.stage == 'GMM_ETRI' or self.stage=='GMMBODY_ETRI':
            if mask_v == 255:
                im_c = im * pcm + (1 - pcm) # [-1,1], fill 1 for other parts
            else:
                im_c = im * pcm - (1 - pcm) # [-1,1], fill 1 for other parts
            im_h = im * phead - (1 - phead) # [-1,1], fill 0 for other parts
        else:
            im_c = im * pcm + (1 - pcm) # [-1,1], fill 1 for other parts
            im_h = im * phead - (1 - phead) # [-1,1], fill 0 for other parts

        # load pose points

        with open(Keypoints_file,'r') as f:
            keypt = json.load(f)
            pose_data = np.array(keypt['landmarks'])
 #       pose_name = im_name.replace('.jpg', '_keypoints.json')
#        with open(osp.join(self.data_path, 'pose', pose_name), 'r') as f:
   #         pose_label = json.load(f)
  #          pose_data = pose_label['people'][0]['pose_keypoints']
    #        pose_data = np.array(pose_data)
     #       pose_data = pose_data.reshape((-1,3))

        point_num = pose_data.shape[0]//3
        #print('point num:', point_num)
        pose_map = torch.zeros(point_num, self.fine_height, self.fine_width)
        r = self.radius
        im_pose = Image.new('L', (self.fine_width, self.fine_height))
        pose_draw = ImageDraw.Draw(im_pose)
        for i in range(point_num):
            one_map = Image.new('L', (self.fine_width, self.fine_height))
            draw = ImageDraw.Draw(one_map)
            pointx = pose_data[i*3]
            pointy = pose_data[i*3+1]
            if pointx > 1 and pointy > 1:
                draw.rectangle((pointx-r, pointy-r, pointx+r, pointy+r), 'white', 'white')
                pose_draw.rectangle((pointx-r, pointy-r, pointx+r, pointy+r), 'white', 'white')
            one_map = self.transform(one_map)
            pose_map[i] = one_map[0]

        #cv2.imshow('pose',im_pose)
        #cv2.waitKey(0)
        # just for visualization
        im_pose = self.transform(im_pose)
        
        # cloth-agnostic representation
        #print('size:', shape.size(), im_h.size(), pose_map.size())
        agnostic = torch.cat([shape, im_h, pose_map], 0) 
        

        if self.stage == 'GMM_ETRI' or self.stage == 'GMMBODY_ETRI':
            im_g = Image.open('grid.png')
            im_g = im_g.resize((self.fine_width, self.fine_height))
            #print('im_g',im_g.size)
            im_g = self.transform(im_g)
        else:
            im_g = ''

        #print('type:', type(im), type(agnostic), type(c), type(cm))
        #print('imgesize:',im.size(), 'agnostic',agnostic.size(), 'cloth',c.size(), 'cloth_mask', cm.size())
        #parse_occ1 = parse_occ[0,:,:]
        #parse_occ1 = parse_occ1[None,:,:]

        result = {
            'c_name':   ann['main_top']+'_F.jpg',     # for visualization
            'im_name':  s_file,    # for visualization or ground truth
            'cloth':    c,          # for input
            'cloth_mask':     cm,   # for input
            'image':    im,         # for visualization
            'agnostic': agnostic,   # for input
            'parse_cloth': im_c,    # for ground truth
            'shape': shape,         # for visualization
            'head': im_h,           # for visualization
            'pose_image': im_pose,  # for visualization
            'grid_image': im_g,     # for visualization
            'cloth_parse': c_parse,
            'occ_parse':parse_occ,
            'c_ori': c_ori
            #'cm_model':parse_cm
            #'occ_parse1':parse_occ1
            }

        return result

    def __len__(self):
        return len(self.ann_list)

class ETRIDataLoader(object):
    def __init__(self, opt, dataset):
        super(ETRIDataLoader, self).__init__()

        if opt.shuffle :
            train_sampler = torch.utils.data.sampler.RandomSampler(dataset)
        else:
            train_sampler = None

        self.data_loader = torch.utils.data.DataLoader(
                dataset, batch_size=opt.batch_size, shuffle=(train_sampler is None),
                num_workers=opt.workers, pin_memory=True, sampler=train_sampler)
        self.dataset = dataset
        self.data_iter = self.data_loader.__iter__()
       
    def next_batch(self):
        try:
            batch = self.data_iter.__next__()
        except StopIteration:
            self.data_iter = self.data_loader.__iter__()
            batch = self.data_iter.__next__()

        return batch


if __name__ == "__main__":
    print("Check the dataset for geometric matching module!")
    
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--dataroot", default = "data")
    parser.add_argument("--datamode", default = "train")
    parser.add_argument("--stage", default = "GMM")
    #parser.add_argument("--data_list", default = "train_pairs.txt")
    parser.add_argument("--fine_width", type=int, default = 192)
    parser.add_argument("--fine_height", type=int, default = 256)
    parser.add_argument("--radius", type=int, default = 3)
    parser.add_argument("--shuffle", action='store_true', help='shuffle input data')
    parser.add_argument('-b', '--batch-size', type=int, default=4)
    parser.add_argument('-j', '--workers', type=int, default=1)
    
    opt = parser.parse_args()
    dataset = CPDataset(opt)
    data_loader = CPDataLoader(opt, dataset)

    print('Size of the dataset: %05d, dataloader: %04d' \
            % (len(dataset), len(data_loader.data_loader)))
    first_item = dataset.__getitem__(0)
    first_batch = data_loader.next_batch()

    from IPython import embed; embed()

