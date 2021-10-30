#coding=utf-8
import torch
import torch.nn as nn
import torch.nn.functional as F

import argparse
import os
import time
from etri_dataset import ETRIDataset, ETRIDataLoader
from networks import GMM_ETRI, UnetGenerator, load_checkpoint, UnetRefinement

from tensorboardX import SummaryWriter
from visualization import board_add_image, board_add_images, save_images_etri, save_parses_etri, save_images
import cv2

# os.environ["CUDA_DEVICE_ORDER"] = "PCI_BUS_ID"
# os.environ["CUDA_VISIBLE_DEVICES"] = "0"

def get_opt():
    parser = argparse.ArgumentParser()
    parser.add_argument("--name", default = "GMM")
    parser.add_argument("--gpu_ids", default = "")
    parser.add_argument('-j', '--workers', type=int, default=1)
    parser.add_argument('-b', '--batch-size', type=int, default=1)
    
    parser.add_argument("--front_or_back", default = 0)
    parser.add_argument("--dataroot", default = "data")
    parser.add_argument("--datamode", default = "test")
    parser.add_argument("--stage", default = "GMM_ETRI")
    parser.add_argument("--fine_width", type=int, default = 384)
    parser.add_argument("--fine_height", type=int, default = 512)
    parser.add_argument("--radius", type=int, default = 5)
    parser.add_argument("--grid_size", type=int, default = 5)
    parser.add_argument('--tensorboard_dir', type=str, default='tensorboard', help='save tensorboard infos')
    parser.add_argument('--result_dir', type=str, default='result', help='save result infos')
    parser.add_argument('--checkpoint', type=str, default='checkpoints/GMM_final_test/step_005000.pth', help='model checkpoint for test')
    parser.add_argument('--checkpointR', type=str, default='', help='model checkpoint for test')
    parser.add_argument("--display_count", type=int, default = 1)
    parser.add_argument("--shuffle", action='store_true', help='shuffle input data')
    parser.add_argument('--posfix_warp', type=str, default='', help='posfix of warping data')

    opt = parser.parse_args()
    return opt

def test_gmm_etri(opt, test_loader, model, board):

    model.cuda()
    model.eval()

    base_name = os.path.basename(opt.checkpoint)
    save_dir = os.path.join(opt.result_dir, base_name, opt.datamode)
    if not os.path.exists(save_dir):
        os.makedirs(save_dir)
    warp_cloth_dir = os.path.join(save_dir, 'warp-cloth')
    if not os.path.exists(warp_cloth_dir):
        os.makedirs(warp_cloth_dir)
    warp_mask_dir = os.path.join(save_dir, 'warp-mask')
    if not os.path.exists(warp_mask_dir):
        os.makedirs(warp_mask_dir)

    for step, inputs in enumerate(test_loader.data_loader):
        iter_start_time = time.time()
        
        c_names = inputs['c_name']
        m_names = inputs['im_name']
        im = inputs['image'].cuda()
        im_pose = inputs['pose_image'].cuda()
        im_h = inputs['head'].cuda()
        shape = inputs['shape'].cuda()
        agnostic = inputs['agnostic'].cuda()
        c = inputs['cloth'].cuda()
        cm = inputs['cloth_mask'].cuda()
        im_c =  inputs['parse_cloth'].cuda()
        im_g = inputs['grid_image'].cuda()
        im_c_parse = inputs['cloth_parse'].cuda()

        c_ori = inputs['c_ori'].cuda()
            
        #print(im_c_parse.shape, cm.shape)
        grid, theta = model(agnostic, c)
        #warped_cloth = F.grid_sample(c, grid, padding_mode='border')
        warped_cloth = F.grid_sample(c_ori, grid, padding_mode='border')
        #print(im_c_parse)#, type(grid), grid.shape)
        warped_mask = F.grid_sample(im_c_parse, grid, padding_mode='zeros')
        warped_grid = F.grid_sample(im_g, grid, padding_mode='zeros')

        visuals = [ [im_h, shape, im_pose], 
                   [c, warped_cloth, im_c], 
                   [warped_grid, (warped_cloth+im)*0.5, im]]
      
        save_images_etri(warped_cloth, c_names, m_names, warp_cloth_dir) 
        save_parses_etri(warped_mask, c_names, m_names, warp_mask_dir)  

        if (step+1) % opt.display_count == 0:
            board_add_images(board, 'combine', visuals, step+1)
            t = time.time() - iter_start_time
            print('step: %8d, time: %.3f' % (step+1, t), flush=True)     

def tensor2img(input_tensor):
     num_channel = input_tensor.size(0)
     vis_tensor = input_tensor.clone()
     min = float(vis_tensor.min())
     max = float(vis_tensor.max())

     vis_tensor.add_(-min).div_(max - min + 1e-5)

     vis_tensor = vis_tensor.mul(255)\
                 .clamp(0, 255)\
                 .byte()\
                 .permute(1, 2, 0)\
                 .cpu().numpy()

     rgb_arr = vis_tensor.copy()
     if num_channel==3:
         rgb_arr[:,:,0] = vis_tensor[:,:,2]
         rgb_arr[:,:,1] = vis_tensor[:,:,1]
         rgb_arr[:,:,2] = vis_tensor[:,:,0]

     return rgb_arr

def test_tom(opt, test_loader, model, board):
    gpus = [int(i) for i in opt.gpu_ids.split(',')]
    model = torch.nn.DataParallel(model, device_ids=gpus).cuda()
    #model.cuda()
    model.eval()
    
    base_name = os.path.basename(opt.checkpoint)
    save_dir = os.path.join(opt.result_dir, base_name, opt.datamode)
    if not os.path.exists(save_dir):
        os.makedirs(save_dir)
    try_on_dir = os.path.join(save_dir, 'try-on')
    if not os.path.exists(try_on_dir):
        os.makedirs(try_on_dir)
    print('Dataset size: %05d!' % (len(test_loader.dataset)), flush=True)
    for step, inputs in enumerate(test_loader.data_loader):
        iter_start_time = time.time()
        
        im_names = inputs['im_name']
        im = inputs['image'].cuda()
        im_pose = inputs['pose_image']
        im_h = inputs['head']
        shape = inputs['shape']

        agnostic = inputs['agnostic'].cuda()
        c = inputs['cloth'].cuda()
        cm = inputs['cloth_mask'].cuda()
        
        outputs = model(torch.cat([agnostic, c],1))
        p_rendered, m_composite = torch.split(outputs, 3,1)
        p_rendered = F.tanh(p_rendered)
        m_composite = F.sigmoid(m_composite)
        p_tryon = c * m_composite + p_rendered * (1 - m_composite)

        visuals = [ [im_h, shape, im_pose], 
                   [c, 2*cm-1, m_composite], 
                   [p_rendered, p_tryon, im]]
            
        save_images(p_tryon, im_names, try_on_dir) 
        #save_images_etri(p_tryon, im_names, try_on_dir)
        if (step+1) % opt.display_count == 0:
            board_add_images(board, 'combine', visuals, step+1)
            t = time.time() - iter_start_time
            print('step: %8d, time: %.3f' % (step+1, t), flush=True)

def main():
    opt = get_opt()
    print(opt)
    print("Start to test stage: %s, named: %s!" % (opt.stage, opt.name))

    # create dataset
    train_dataset = ETRIDataset(opt)

    # create dataloader
    train_loader = ETRIDataLoader(opt, train_dataset)

    # visualization
    if not os.path.exists(opt.tensorboard_dir):
        os.makedirs(opt.tensorboard_dir)
    board = SummaryWriter(log_dir = os.path.join(opt.tensorboard_dir, opt.name))
   
    # create model & train
    if opt.stage == 'GMM_ETRI':
        model = GMM_ETRI(opt)
        gpus = [int(i) for i in opt.gpu_ids.split(',')]
        model = torch.nn.DataParallel(model, device_ids=gpus).cuda()
        load_checkpoint(model, opt.checkpoint)
        with torch.no_grad():
            test_gmm_etri(opt, train_loader, model, board)
    elif opt.stage == 'GMMBODY_ETRI':
        model = GMM_ETRI(opt)
        gpus = [int(i) for i in opt.gpu_ids.split(',')]
        model = torch.nn.DataParallel(model, device_ids=gpus).cuda()
        load_checkpoint(model, opt.checkpoint)
        with torch.no_grad():
            test_gmm_etri(opt, train_loader, model, board)
    elif opt.stage == 'TOM':
        model = UnetGenerator(25, 4, 6, ngf=64, norm_layer=nn.InstanceNorm2d)
        load_checkpoint(model, opt.checkpoint)
        print('*********checkpoint',opt.checkpoint)
        with torch.no_grad():
            test_tom(opt, train_loader, model, board)
    elif opt.stage == 'TOM_SNPATCH_ETRI':
        Gmodel = UnetGenerator(26, 4, 7, ngf=64, norm_layer=nn.InstanceNorm2d)
        Gmodel = torch.nn.DataParallel(Gmodel)
        #Dmodel = SNPatchDiscriminator()
        #print('Discriminator, ',Dmodel)
        if not opt.checkpoint == '' and os.path.exists(opt.checkpoint):
            load_checkpoint(Gmodel, opt.checkpoint)

        with torch.no_grad():
            test_tom(opt,train_loader,Gmodel,board)
        #train_etri_tom_snpatchd(opt, train_loader, Gmodel,  Dmodel, board)
        #save_checkpoint(Gmodel, os.path.join(opt.checkpoint_dir, opt.name, 'tom_G_etri.pth'))
        #save_checkpoint(Dmodel, os.path.join(opt.checkpoint_dir, opt.name, 'tom_D_etri.pth'))
        
    else:
        raise NotImplementedError('Model [%s] is not implemented' % opt.stage)
  
    print('Finished test %s, named: %s!' % (opt.stage, opt.name))

if __name__ == "__main__":
    main()
