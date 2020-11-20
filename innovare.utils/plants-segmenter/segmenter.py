import csv
import pathlib

import cv2
import matplotlib.pyplot
import numpy as np
import os
import glob
import hashlib
import argparse

def get_hash(f):
    return hashlib.md5(f.read_bytes()).hexdigest()

my_parser = argparse.ArgumentParser(fromfile_prefix_chars='@')

my_parser.add_argument('--delivery_base',
                       action='store',
                       default='http://ditai.cerict/',
                       type=str,
                       help='base delivery for gernated links')

my_parser.add_argument('--srcdir',
                       action='store',
                       type=str,
                       help='source images dir',
                       required=True)

my_parser.add_argument('--dstdir',
                       required=True,
                       help='destination images dir')

my_parser.add_argument('--eroded_background',
                       type=int,
                       default=1,
                       help='include eroded background cuts near to original cuts')

my_parser.add_argument('--area_threshold',
                       type=float,
                       default=0.35,
                       help='skip images with plant area ration above threshold')

my_parser.add_argument('--focus_threshold',
                       type=int,
                       default=600,
                       help='skip images with plant area focus below threshold')

my_parser.add_argument('--max_plant_box_size',
                       type=int,
                       default=1200,
                       help='size of extracted plants')

my_parser.add_argument('--show_all_boxes',
                       type=int,
                       default=0,
                       help='show all the boxes, regardless of sizes')

my_parser.add_argument('--skip_unbalanced_cuts',
                       type=int,
                       default=1,
                       help='skip unbalanced cuts')

my_parser.add_argument('--image_processing_limit',
                       type=int,
                       default=1500,
                       help='limit the number of processed images')

my_parser.add_argument('--clean_destination',
                       type=int,
                       default=0,
                       help='clean destination dir before start (use with caution)')

my_parser.add_argument('--normalize',
                       action='store_true',
                       help='if src images needs min-max normalization')

my_parser.add_argument('--create_hash_symlinks',
                       action='store_true',
                       help='to create symlinks to plant image files hash')

my_parser.add_argument('-v',
                       '--verbose',
                       action='store_true',
                       help='an optional argument')

# Execute parse_args()
args = my_parser.parse_args()

srcdir = args.srcdir
dstdir = args.dstdir

g_max_plant_size = args.max_plant_box_size
g_focus_degree = args.focus_threshold
g_include_black_background = args.eroded_background
g_skip_area_threshold = args.area_threshold
g_show_all_boxes=args.show_all_boxes
g_skip_unbalanced_cuts=args.skip_unbalanced_cuts
g_delete_destination=args.clean_destination
g_normalize = args.normalize
g_image_processing_limit = args.image_processing_limit

if args.verbose:
    print("Source directory:" + srcdir)
    print("Destination directory:" + dstdir)
    print("Settings:\n\
    max_plant_size = {0}\n\
    focus_degree = {1}\n\
    include_black_background = {2}\n\
    skip_area_threshold = {3}\n\
    show_all_boxes = {4}\n\
    skip_unbalanced_cuts = {4}\n\
    delete_destination = {5}\n".format(g_max_plant_size,g_focus_degree,g_include_black_background,g_skip_area_threshold,
                                       g_show_all_boxes,g_skip_unbalanced_cuts,g_delete_destination))

srcParam = srcdir+os.sep+"*"
flight_session_dirs = glob.glob(srcParam,recursive=True)

if args.verbose:
    print("Flight session dirs:" + str(flight_session_dirs))
    if len(flight_session_dirs) == 0:
        print("Nothing to do. Terminating.")

if not os.path.exists(dstdir):
    print("Error: destination dir does not exists.")

processed_images_count = 0
global_generated_images_counter = 0
metadata = []

def generate_metadata():
    if args.verbose:
        print("Writing metadata....")
    csv_columns = [
        'date',
        'kind',
        'rows',
        'session',
        'photo-id',
        'plant-id',
        'filename',
        'hash',
        'link'
    ]
    csv_file = dstdir + os.sep + "metadata.csv"
    try:
        with open(csv_file, 'w') as csvfile:
            writer = csv.DictWriter(csvfile, fieldnames=csv_columns)
            writer.writeheader()
            for data in metadata:
                writer.writerow(data)
    except IOError:
        print("I/O error")

for flight_session_dir in flight_session_dirs:

    flight_session_optical_files = glob.glob(flight_session_dir+os.sep+"Thermal_Optical/*/*/*.jpg",recursive=True)

    if args.verbose:
        print(flight_session_dir)
        print(flight_session_optical_files)

    for flight_session_optical_file in flight_session_optical_files:

        processed_images_count = processed_images_count + 1
        # if hit hard limit exit.
        if processed_images_count > g_image_processing_limit:
            if args.verbose:
                print("Hit hard image processing limit of {0} images. Stopping.".format(g_image_processing_limit))
            generate_metadata()
            exit(0)

        if args.verbose:
            print("Processing image:"+flight_session_optical_file+" - number="+str(processed_images_count))

        dst_file_name_path = flight_session_optical_file.replace(srcdir, "")\
                                                                    .replace("_Dataset_FontanaDeiFieri/","_")\
                                                                    .replace("./", "")\
                                                                    .replace(os.sep, "-")\
                                                                    .replace(".jpg", "")[1:]
        dst_path_prefix = dstdir + os.sep + dst_file_name_path
        print("Base:" + dst_path_prefix)

        segments = dst_file_name_path.split("-")

        src = cv2.imread(flight_session_optical_file)

        if g_normalize:
            if args.verbose:
                print("Normalizing image...")
            n1=np.array(src,dtype='float64')
            n2 = np.zeros((3000,4000,3))
            cv2.normalize(n1,n2,0,255,cv2.NORM_MINMAX)
            img = np.array(n2,dtype='float64')
            src = np.array(n2,dtype='float64')
        else:
            img = np.array(src,dtype='float64')

        imgRectWithBg = src.copy()

        b = np.zeros((img.shape[0],img.shape[1]), dtype=img.dtype)
        g = np.zeros((img.shape[0],img.shape[1]), dtype=img.dtype)
        r = np.zeros((img.shape[0],img.shape[1]), dtype=img.dtype)
        b[:,:] = img[:,:,0]
        g[:,:] = img[:,:,1]
        r[:,:] = img[:,:,2]

        new=2*g-r-b
        w=new.min()+50
        e=255
        new=new-w
        new=new/e*255
        new=np.array(new,dtype='uint8')

        cv2.imwrite(dst_path_prefix + "_grayed-thresholded.jpg", (new * 255).astype(np.uint8))
        ret2, th2 = cv2.threshold(new, w, e, cv2.THRESH_BINARY+cv2.THRESH_OTSU)
        hole=th2.copy()
        cv2.floodFill(hole,None,(0,0),255)
        hole=cv2.bitwise_not(hole)
        filledEdgesOut=cv2.bitwise_or(th2,hole)
        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE,(10,10))
        eroded = cv2.erode(filledEdgesOut,kernel)

        def getConnectedComponentsInfo(image, size):
            output=image.copy()
            nlabels, labels, stats, centroids = cv2.connectedComponentsWithStats(image)
            for i in range(1,nlabels-1):
                regions_size=stats[i,4]
                if regions_size<size:
                    x0=stats[i,0]
                    y0=stats[i,1]
                    x1=stats[i,0]+stats[i,2]
                    y1=stats[i,1]+stats[i,3]
                    for row in range(y0,y1):
                        for col in range(x0,x1):
                            if labels[row,col]==i:
                                output[row,col]=0
            return output

        eroded_image=getConnectedComponentsInfo(eroded, 180)

        print("Number of pixels in the plant:", len(eroded_image.nonzero()[0]))
        distance_top=50
        Area=(pow((0.000122*(distance_top-0.304)/0.304),2) * len(eroded_image.nonzero()[0]))
        print("Leaf area:",round(Area, 2))

        if round(Area,2) > 1500:
            print("WARNING: image lights not in range skipping:"+dst_path_prefix)
            continue

        # Compute new image
        img[:,:,2]= eroded_image * r
        img[:,:,1]= eroded_image * g
        img[:,:,0]= eroded_image * b

        clips = []

        # Recursive cut
        def recursiveCut(imgRect,x,y,h,w,all=0,cleancuts=0,verbose=0):
                global imageId
                global imgRectWithBg
                if h>250 and w>250:
                    if abs(w-h)<90:
                        if verbose==1:
                            print("SQ X:{0} Y:{1} W:{2} H:{3}".format(x,y,w,h))
                        cv2.rectangle(imgRect, (x, y), (x+w, y+h), (0, 0, 255), thickness=5)
                        cv2.putText(imgRect, "Identifier: p{}".format(imageId), (x+30, y+30),cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 2)
                        clips.append((x,y,w,h))
                        imageId=imageId+1
                    else:
                        if h>w:
                            mh = int(h/2)
                            if cleancuts==1:
                                recursiveCut(imgRect,x, y, mh-10 , w , all)
                                recursiveCut(imgRect,x, y + mh +10, mh , w , all)
                        else:
                            if verbose==1:
                                print("HO X:{0} Y:{1} W:{2} H:{3}".format(x,y,w,h))
                            cv2.rectangle(imgRect, (x, y), (x+w, y+h), (0, 0, 255), thickness=5)
                            cv2.putText(imgRect, "Identifier: p{}".format(imageId), (x+30, y+30),cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 2)
                            clips.append((x,y,w,h))
                            imageId=imageId+1
                else:
                    if all==1:
                        cv2.rectangle(imgRect, (x, y), (x+w, y+h), (255, 0, 0), thickness=5)

        if g_include_black_background==1:
            clipErodedSrc = (img * 255).astype(np.uint8).copy()
        clipSrc = src.copy()
        imgRect = (img * 255).astype(np.uint8).copy()
        n_labels, labels, stats, centroids = cv2.connectedComponentsWithStats(th2)
        print(n_labels)
        size_thresh = 1
        imageId = 0
        for i in range(1, n_labels):
            if stats[i, cv2.CC_STAT_AREA] >= size_thresh:
                px = stats[i, cv2.CC_STAT_LEFT]
                py = stats[i, cv2.CC_STAT_TOP]
                pw = stats[i, cv2.CC_STAT_WIDTH]
                ph = stats[i, cv2.CC_STAT_HEIGHT]
                recursiveCut(imgRect, px, py, ph, pw, g_show_all_boxes, g_skip_unbalanced_cuts)

        maxsz =0
        ctr=0
        for clip in clips:
            print("Clip "+str(ctr)+" :"+str(clip))
            maxsz= max(clip[2],clip[3],maxsz)
            ctr=ctr+1
        print(maxsz)

        if (maxsz > g_max_plant_size):
            print("Warning: plant greater then imposed size")

        maxsz = g_max_plant_size

        def variance_of_laplacian(image):
            # compute the Laplacian of the image and then return the focus
            # measure, which is simply the variance of the Laplacian
            return cv2.Laplacian(image, cv2.CV_64F).var()

        counter = 0
        skipped_image_counter = 0
        for clip in clips:
            x=clip[0]
            y=clip[1]
            w=clip[2]
            h=clip[3]
            roi_original = np.zeros((maxsz, maxsz, 3))
            roi_eroded = np.zeros((maxsz, maxsz, 3))
            print(clip)
            print("Imgn: " + str(roi_original.shape))
            print("Imgn[0-h,0-w,:]: " + str(roi_original[0:h, 0:w, :].shape))
            print("clipSrc[....]: "+str(clipSrc[y:y+h,x:x+w,:].shape))
            if not roi_original[0:h, 0:w, :].shape==clipSrc[y:y+h,x:x+w,:].shape:
                print("ROI of original image not conformant to clipSrc. Skipping")
                continue
            clip_from_original = clipSrc[y:y + h, x:x + w, :]
            if g_include_black_background==1:
                clip_from_eroded = clipErodedSrc[y:y + h, x:x + w, :]
            sw = int((g_max_plant_size - w) / 2)
            sh = int((g_max_plant_size - h) / 2)
            roi_original[sh:sh + clip_from_original.shape[0], sw:sw + clip_from_original.shape[1], :]=clip_from_original
            if g_include_black_background==1:
                roi_eroded[sh:sh + clip_from_eroded.shape[0], sw:sw + clip_from_eroded.shape[1], :]=clip_from_eroded
            print("number of pixels of the plant    :", len(roi_original.nonzero()[0]))
            print("number of pixels not of the plant:", g_max_plant_size * g_max_plant_size)
            arearatio = len(roi_original.nonzero()[0]) / (g_max_plant_size * g_max_plant_size)
            print("number of pixels ratio           :",arearatio)
            distance_top=50
            Area=(pow((0.000122*(distance_top-0.304)/0.304),2) * len(roi_original.nonzero()[0]))
            print("leaf area:{0}".format(str(round(Area, 2))))
            focused = True
            var = variance_of_laplacian(roi_original)
            print("Focus degree:"+str(var))
            if var < g_focus_degree:
                print("Skipped image for not in focus:"+"c"+str(counter)+".jpg\n")
                cv2.rectangle(imgRect, (x, y), (x+w, y+h), (0, 255, 255), thickness=5)
                skipped_image_counter+=1
            else:
                if arearatio >= g_skip_area_threshold:
                    print("Saved image:"+dst_path_prefix + "-plant_" + str(counter) + ".jpg\n")
                    roi_original_image_path = dst_path_prefix + "-plant_" + str(counter) + ".jpg"
                    roi_filename = dst_file_name_path + "-plant_" + str(counter) + ".jpg"
                    cv2.imwrite(roi_original_image_path, roi_original)
                    roi_hash = hashlib.md5(pathlib.Path(roi_original_image_path).read_bytes()).hexdigest()
                    metadata.append({
                        'date': segments[0][0:10],
                        'kind': segments[0][11:26],
                        'rows': segments[1][5:10],
                        'session': segments[2][7:8],
                        'photo-id': segments[3],
                        'plant-id': counter,
                        'filename': roi_filename,
                        'hash': roi_hash,
                        'link': args.delivery_base + "id_"+roi_hash+".jpg"
                    })
                    if args.create_hash_symlinks:
                        if args.verbose:
                            print("Creating hash symlink:"+dstdir+os.sep+"id_"+roi_hash+".jpg")
                        if os.path.islink(dstdir+os.sep+"id_"+roi_hash+".jpg"):
                            print("Removing existing link:"+dstdir+os.sep+"id_"+roi_hash+".jpg")
                            os.unlink(dstdir+os.sep+"id_"+roi_hash+".jpg")
                        os.symlink(roi_filename,dstdir+os.sep+"id_"+roi_hash+".jpg")
                    if g_include_black_background==1:
                        print("Saved image:" + dst_path_prefix + "-erased_background_plant_" + str(counter) + ".jpg\n")
                        cv2.imwrite(dst_path_prefix + "-erased_background_plant_" + str(counter) + ".jpg", roi_eroded)
                else:
                    print("Skipped image for area threshold:"+"c"+str(counter)+".jpg\n")
                    cv2.rectangle(imgRect, (x, y), (x+w, y+h), (0, 255, 255), thickness=5)
                    skipped_image_counter += 1
            counter += 1

        global_generated_images_counter += counter - skipped_image_counter
        if args.verbose:
            print("Generated images for current image:" + str(counter - skipped_image_counter))
            print("Generated image for so far:" + str(global_generated_images_counter))
        cv2.imwrite(dst_path_prefix + "-erased_background_with_boxes.jpg", imgRect)
        cv2.imwrite(dst_path_prefix + "-erased_background.jpg", (img * 255).astype(np.uint8))
        #---------------------------------------------------------------------------------

generate_metadata()