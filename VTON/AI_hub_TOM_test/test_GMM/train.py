#coding=utf-8
#region [import Package]
import torch
import torch.nn as nn
import torch.nn.functional as F

import argparse
import os
import time
from etri_dataset import ETRIDataset, ETRIDataLoader
from networks import GMM, UnetGenerator, VGGLoss, load_checkpoint, save_checkpoint, Discriminator, SNPatchDiscriminator, UnetRefinement, save_coarse_refine_checkpoints, GMM_ETRI

from tensorboardX import SummaryWriter
from visualization import board_add_image, board_add_images

from torch.autograd import Variable

import cv2
import numpy as np
#endregion

os.environ["CUDA_DEVICE_ORDER"]="PCI_BUS_ID"
os.environ["CUDA_VISIBLE_DEVICES"]="0"

#region [Get_Opt]
def get_opt():
    parser = argparse.ArgumentParser()
    parser.add_argument("--name", default="GMM")
    parser.add_argument("--gpu_ids", default="0")
    parser.add_argument('-j', '--workers', type=int, default=1)
    parser.add_argument('-b', '--batch-size', type=int, default=4)

    parser.add_argument("--dataroot", default="data")
    parser.add_argument("--datamode", default="train")
    parser.add_argument("--stage", default="GMM_ETRI")
    # parser.add_argument("--data_list", default = "train_pairs.txt")
    parser.add_argument("--fine_width", type=int, default=384)
    parser.add_argument("--fine_height", type=int, default=512)
    parser.add_argument("--radius", type=int, default=5)
    parser.add_argument("--grid_size", type=int, default=5)
    parser.add_argument('--lr', type=float, default=0.0001, help='initial learning rate for adam')
    parser.add_argument('--tensorboard_dir', type=str, default='tensorboard', help='save tensorboard infos')
    parser.add_argument('--checkpoint_dir', type=str, default='checkpoints', help='save checkpoint infos')
    parser.add_argument('--checkpoint', type=str, default='', help='model checkpoint for initialization')
    parser.add_argument("--display_count", type=int, default=20)
    parser.add_argument("--save_count", type=int, default=5000)
    parser.add_argument("--keep_step", type=int, default=100000)
    parser.add_argument("--decay_step", type=int, default=100000)
    parser.add_argument("--shuffle", action='store_true', help='shuffle input data')
    parser.add_argument("--iteration", type=int, default=3)
    parser.add_argument("--front_or_back", type=int, default=0, help='front =0, back =1')
    parser.add_argument('--posfix_warp', type=str, default='', help='posfix of warping data')

    opt = parser.parse_args()
    return opt
#endregion

#region [Train_GMM]
def train_gmm_etri(opt, train_loader, model, board):
    gpus = [int(i) for i in opt.gpu_ids.split(',')]

    model = torch.nn.DataParallel(model, device_ids=gpus).cuda()

    model.train()

    # criterion
    criterionL1 = nn.L1Loss()

    # optimizer
    optimizer = torch.optim.Adam(model.parameters(), lr=opt.lr, betas=(0.5, 0.999))
    scheduler = torch.optim.lr_scheduler.LambdaLR(optimizer, lr_lambda=lambda step: 1.0 -
                                                                                    max(0,
                                                                                        step - opt.keep_step) / float(
        opt.decay_step + 1))

    # << [ SC : prepare GIC ]
    grid_size = [16, 12]

    gridlength = [int(opt.fine_height / grid_size[0]), int(opt.fine_width / grid_size[1])]

    gridpts = np.zeros((grid_size[0], grid_size[1], 2), dtype=np.int)
    for h in range(grid_size[0]):
        for w in range(grid_size[1]):
            gridpts[h, w] = np.array([gridlength[0] * h, gridlength[1] * w], dtype=np.int)

    for step in range(opt.keep_step + opt.decay_step):
        iter_start_time = time.time()
        inputs = train_loader.next_batch()

        im = inputs['image'].cuda()
        im_pose = inputs['pose_image'].cuda()
        im_h = inputs['head'].cuda()
        shape = inputs['shape'].cuda()
        agnostic = inputs['agnostic'].cuda()
        c = inputs['cloth'].cuda()
        cm = inputs['cloth_mask'].cuda()
        im_c = inputs['parse_cloth'].cuda()
        im_g = inputs['grid_image'].cuda()
        im_occ = inputs['occ_parse'].cuda()

        grid, theta = model(agnostic, c)

        ##<< [ (START) SC : GIC ]
        dist_gic = torch.zeros([1], dtype=torch.float32).cuda()

        alpha = 0.0
        if opt.stage == 'GMM_ETRI':
            alpha = 0.002
        elif opt.stage == 'GMMBODY_ETRI':
            alpha = 0.001

        for h in range(1, grid_size[0] - 1):
            for w in range(1, grid_size[1] - 1):
                bw = gridpts[h, w - 1]
                ww = gridpts[h, w]
                aw = gridpts[h, w + 1]

                dist_wb = torch.dist(grid[:, bw[0], bw[1], :], grid[:, ww[0], ww[1], :])
                dist_wa = torch.dist(grid[:, aw[0], aw[1], :], grid[:, ww[0], ww[1], :])

                dist_w = torch.sum(torch.abs(torch.subtract(dist_wb, dist_wa)))

                bh = gridpts[h - 1, w]
                hh = gridpts[h, w]
                ah = gridpts[h + 1, w]

                dist_hb = torch.dist(grid[:, bh[0], bh[1], :], grid[:, hh[0], hh[1], :])
                dist_ha = torch.dist(grid[:, ah[0], ah[1], :], grid[:, hh[0], hh[1], :])

                dist_h = torch.sum(torch.abs(torch.subtract(dist_hb, dist_ha)))

                dist_gic += dist_w + dist_h
        ##<< [ (END) SC : GIC ]

        warped_cloth = F.grid_sample(c, grid, padding_mode='border', align_corners=True)
        warped_mask = F.grid_sample(cm, grid, padding_mode='zeros', align_corners=True)
        warped_grid = F.grid_sample(im_g, grid, padding_mode='zeros')

        warped_cloth *= im_occ
        im_c *= im_occ

        visuals = [[im_h, shape, im_pose],
                   [c, warped_cloth, im_c],
                   [warped_grid, (warped_cloth + im) * 0.5, im]]

        cloth_img_loss = criterionL1(warped_cloth, im_c)
        # cloth_parse_loss = criterionL1(warped_mask, cm_model)
        gic_loss = alpha * dist_gic
        loss = cloth_img_loss + gic_loss
        optimizer.zero_grad()
        loss.backward()
        optimizer.step()

        if (step + 1) % opt.display_count == 0:
            board_add_images(board, 'combine', visuals, step + 1)
            board.add_scalar('metric', loss.item(), step + 1)
            t = time.time() - iter_start_time
            print('step: %8d, time: %.3f, loss: %4f, imgloss: %4f, gicloss: %4f' % (
            step + 1, t, loss.item(), cloth_img_loss.item(), alpha * dist_gic), flush=True)

        if (step + 1) % opt.save_count == 0:
            save_checkpoint(model, os.path.join(opt.checkpoint_dir, opt.name, 'step_%06d.pth' % (step + 1)))
#endregion

# region [Train_TOM]
def train_etri_tom_snpatchd(opt, train_loader, Gmodel, Dmodel, board):

    gpus = [int(i) for i in opt.gpu_ids.split(',')]

    Gmodel = torch.nn.DataParallel(Gmodel, device_ids=gpus).cuda()
    Dmodel = torch.nn.DataParallel(Dmodel, device_ids=gpus).cuda()

    Gmodel.train()
    Dmodel.train()

    # criterion
    criterionL1 = nn.L1Loss()
    criterionVGG = VGGLoss()
    criterionMask = nn.L1Loss()
    criterionBCE = nn.BCELoss()

    # optimizer
    Goptimizer = torch.optim.Adam(Gmodel.parameters(), lr=opt.lr, betas=(0.5, 0.999))
    Gscheduler = torch.optim.lr_scheduler.LambdaLR(Goptimizer, lr_lambda=lambda step: 1.0 -
                                                                                    max(0,
                                                                                        step - opt.keep_step) / float(
        opt.decay_step + 1))

    Doptimizer = torch.optim.Adam(Dmodel.parameters(), lr=opt.lr, betas=(0.5, 0.999))
    Dscheduler = torch.optim.lr_scheduler.LambdaLR(Doptimizer, lr_lambda=lambda step: 1.0 -
                                                                                      max(0,
                                                                                          step - opt.keep_step) / float(
        opt.decay_step + 1))

    train_discriminator = False

    for step in range(int((opt.keep_step + opt.decay_step))):

        iter_start_time = time.time()
        inputs = train_loader.next_batch()

        im = inputs['image'].cuda()
        im_pose = inputs['pose_image']
        im_h = inputs['head']
        shape = inputs['shape']
        agnostic = inputs['agnostic'].cuda()
        c = inputs['cloth'].cuda()
        cm = inputs['cloth_mask'].cuda()

        ##<< (START) [ SC : TRAINING DISCRIMINATOR ]
        real_images = Variable(im)
        real_labels = Variable((torch.ones([im.size(0),1])-0.1).cuda())

        outputs = Gmodel(torch.cat([agnostic, c], 1))
        p_rendered, m_composite = torch.split(outputs, 3, 1)
        p_rendered = torch.tanh(p_rendered)
        m_composite = torch.sigmoid(m_composite)

        m_composite2 = m_composite.clone()

        fake_images = c * m_composite + p_rendered * (1 - m_composite)
        p_viton = fake_images.clone()
        fake_labels = Variable(torch.zeros([im.size(0),1]).cuda())

        Dmodel.zero_grad()

        real_outputs = Dmodel(real_images)
        real_loss = criterionBCE(real_outputs, real_labels)
        real_score = real_outputs

        fake_outputs = Dmodel(fake_images.detach())
        fake_loss = criterionBCE(fake_outputs, fake_labels)
        fake_score = fake_outputs

        Dloss = real_loss + fake_loss
        #if train_discriminator:
        Doptimizer.zero_grad()
        Dloss.backward()
        Doptimizer.step()
        ##<< (END) [ SC : TRAINING DISCRIMINATOR ]

        # ##<< (START) [ SC : TRAINING GENERATOR ]
        Gmodel.zero_grad()

        #discri_guesses = Dmodel(p_viton,binary_shape)
        discri_guesses = Dmodel(p_viton)

        loss_l1 = criterionL1(p_viton, im)
        loss_vgg = criterionVGG(p_viton, im)
        loss_mask = criterionMask(m_composite2, cm)
        all_trues = Variable((torch.ones([im.size(0),1])-0.1).cuda())
        ALPHA_adv = 0.01

        loss_adversarial = criterionBCE(discri_guesses,all_trues) * ALPHA_adv

        Gloss = loss_l1 + loss_vgg + loss_mask + loss_adversarial
        #if train_discriminator == False:
        Goptimizer.zero_grad()
        Gloss.backward()
        Goptimizer.step()
        ##<< (END) [ SC : TRAINING GENERATOR ]

        visuals = [[im_h, shape, im_pose],
                   [c, cm * 2 - 1, m_composite * 2 - 1],
                   [p_rendered, fake_images, im]]

        if (step + 1) % opt.display_count == 0:
            board_add_images(board, 'combine', visuals, step + 1)
            board.add_scalar('metric', Gloss.item(), step + 1)
            board.add_scalar('L1', loss_l1.item(), step + 1)
            board.add_scalar('VGG', loss_vgg.item(), step + 1)
            board.add_scalar('MaskL1', loss_mask.item(), step + 1)
            board.add_scalar('V_Loss', (loss_l1+loss_vgg).item(), step + 1)
            board.add_scalar('AdversarialLoss',loss_adversarial.item(), step+1)
            board.add_scalar('DiscriminatorLoss:', Dloss.item(), step + 1)
            t = time.time() - iter_start_time

            print('step: %8d, time: %.3f, Gloss: %.4f, vloss: %.4f, l1: %.4f, vgg: %.4f, mask: %.4f, advers: %.4f / discrim: %.4f'
                  % (step + 1, t, Gloss.item(), (loss_l1+loss_vgg).item(), loss_l1.item(),
                     loss_vgg.item(), loss_mask.item(), loss_adversarial.item(),Dloss.item()), flush=True)

        if (step + 1) % opt.save_count == 0:
            save_checkpoint(Gmodel, os.path.join(opt.checkpoint_dir, opt.name, 'step_%06d_G.pth' % (step + 1)))
            save_checkpoint(Dmodel, os.path.join(opt.checkpoint_dir, opt.name, 'step_%06d_D.pth' % (step + 1)))
# endregion

#region [Main]
def main():
    opt = get_opt()
    print(opt)
    print("Start to train stage: %s, named: %s!" % (opt.stage, opt.name))

    # create dataset
    train_dataset = ETRIDataset(opt)

    # create dataloader
    train_loader = ETRIDataLoader(opt, train_dataset)

    # visualization
    if not os.path.exists(opt.tensorboard_dir):
        os.makedirs(opt.tensorboard_dir)
    board = SummaryWriter(log_dir=os.path.join(opt.tensorboard_dir, opt.name))

    # create model & train & save the final checkpoint
    if opt.stage == 'GMM_ETRI':
        model = GMM_ETRI(opt)
        if not opt.checkpoint == '' and os.path.exists(opt.checkpoint):
            load_checkpoint(model, opt.checkpoint)
        train_gmm_etri(opt, train_loader, model, board)
        save_checkpoint(model, os.path.join(opt.checkpoint_dir, opt.name, 'gmm_etri.pth'))

    elif opt.stage == 'GMMBODY_ETRI':
        model = GMM_ETRI(opt)
        if not opt.checkpoint == '' and os.path.exists(opt.checkpoint):
            load_checkpoint(model, opt.checkpoint)
        train_gmm_etri(opt, train_loader, model, board)
        save_checkpoint(model, os.path.join(opt.checkpoint_dir, opt.name, 'gmm_etri.pth'))

    elif opt.stage == 'TOM_SNPATCH_ETRI':
        Gmodel = UnetGenerator(26, 4, 7, ngf=64, norm_layer=nn.InstanceNorm2d)
        # Gmodel = torch.nn.DataParallel(Gmodel)
        Dmodel = SNPatchDiscriminator()
        print('Discriminator, ', Dmodel)
        if not opt.checkpoint == '' and os.path.exists(opt.checkpoint):
            load_checkpoint(Gmodel, opt.checkpoint)
        train_etri_tom_snpatchd(opt, train_loader, Gmodel, Dmodel, board)
        save_checkpoint(Gmodel, os.path.join(opt.checkpoint_dir, opt.name, 'tom_G_etri.pth'))
        save_checkpoint(Dmodel, os.path.join(opt.checkpoint_dir, opt.name, 'tom_D_etri.pth'))



    else:
        raise NotImplementedError('Model [%s] is not implemented' % opt.stage)

    print('Finished training %s, nameed: %s!' % (opt.stage, opt.name))


if __name__ == "__main__":
    main()
#endregion


