import numpy as np
import cv2
from matplotlib import pyplot as plt

STAGE_FIRST_FRAME = 0
STAGE_SECOND_FRAME = 1
STAGE_DEFAULT_FRAME = 2
kMinNumFeature = 1500
MATCHING_DIST_THRESHOLD = 1
MATCHING_NN = 2
MATCHING_NNDR = 3

lk_params = dict(winSize  = (21, 21),
                 criteria = (cv2.TERM_CRITERIA_EPS | cv2.TERM_CRITERIA_COUNT, 30, 0.01))

def featureTracking(image_ref, image_cur, px_ref):
    kp2, st, err = cv2.calcOpticalFlowPyrLK(image_ref, image_cur, px_ref, None, **lk_params)  #shape: [k,2] [k,1] [k,1]
    st = st.reshape(st.shape[0])
    kp1 = px_ref[st == 1]
    kp2 = kp2[st == 1]
    return kp1, kp2

#Task 2: overall organisation of this function (no marks)
def featureMatching(image_ref, image_cur, matching_algorithm, threshold_value, output_path):
    #The following line creates a SIFT detector
    detector = cv2.xfeatures2d.SIFT_create(nfeatures=kMinNumFeature)

    #LAB 4: put your feature matching code after this line

    #The following line will write the distance values of feature matches to an output file
    #Change the 1st parameter to the variable holding the feature matches obtained by your implementation
    printMatchesToFile(None, output_path)

    #The following lines will display the two images side by side. Make sure that you change the 2nd, 4th and 5th parameters
    #The 2nd parameter should be the keypoints detected in the previous image
    #The 4th parameter should be the keypoints detected in the current image
    #The 5th parameter should be the matches
    #NOTE: Depending on which BFMatcher function you are using for feature matching, you might want to consider calling drawMatchesKnn instead
    img3 = cv2.drawMatches(image_ref, None, image_cur, None, None, None, flags=2)
    fig = plt.figure()
    fig.set_size_inches(10,3)
    plt.imshow(img3)
    plt.show()

def printMatchesToFile(matches, output_path):
    #Write your code for generating the output file following the required format
    output_file = open(output_path, 'w')
    output_file.close()

class PinholeCamera:
    def __init__(self, width, height, fx, fy, cx, cy,
                k1=0.0, k2=0.0, p1=0.0, p2=0.0, k3=0.0):
        self.width = width
        self.height = height
        self.fx = fx
        self.fy = fy
        self.cx = cx
        self.cy = cy
        self.distortion = (abs(k1) > 0.0000001)
        self.d = [k1, k2, p1, p2, k3]


class VisualOdometry:
    def __init__(self, cam, annotations):
        self.frame_stage = 0
        self.cam = cam
        self.new_frame = None
        self.last_frame = None
        self.cur_R = None
        self.cur_t = None
        self.px_ref = None
        self.px_cur = None
        self.kps = None
        self.desc = None
        self.focal = cam.fx
        self.pp = (cam.cx, cam.cy)
        self.trueX, self.trueY, self.trueZ = 0, 0, 0
        self.detector =  cv2.xfeatures2d.SIFT_create(nfeatures=kMinNumFeature)
        with open(annotations) as f:
            self.annotations = f.readlines()

    def getAbsoluteScale(self, frame_id):  #specialized for KITTI odometry dataset
        ss = self.annotations[frame_id-1].strip().split()
        x_prev = float(ss[3])
        y_prev = float(ss[7])
        z_prev = float(ss[11])
        ss = self.annotations[frame_id].strip().split()
        x = float(ss[3])
        y = float(ss[7])
        z = float(ss[11])
        self.trueX, self.trueY, self.trueZ = x, y, z
        return np.sqrt((x - x_prev)*(x - x_prev) + (y - y_prev)*(y - y_prev) + (z - z_prev)*(z - z_prev))

    def processFirstFrame(self):
        self.px_ref, self.desc = self.detector.detectAndCompute(self.new_frame, None)
        self.kps = self.px_ref
        self.px_ref = np.array([x.pt for x in self.px_ref], dtype=np.float32)
        self.frame_stage = STAGE_SECOND_FRAME

    def processSecondFrame(self, test_frame_id, matching_algorithm, threshold_value, output_path):
        self.px_ref, self.px_cur = featureTracking(self.last_frame, self.new_frame, self.px_ref)
        if (test_frame_id == 1):
            featureMatching(self.last_frame, self.new_frame, matching_algorithm, threshold_value, output_path)
        E, mask = cv2.findEssentialMat(self.px_cur, self.px_ref, focal=self.focal, pp=self.pp, method=cv2.RANSAC, prob=0.999, threshold=1.0)
        _, self.cur_R, self.cur_t, mask = cv2.recoverPose(E, self.px_cur, self.px_ref, focal=self.focal, pp = self.pp)
        self.frame_stage = STAGE_DEFAULT_FRAME
        self.px_ref = self.px_cur

    def processFrame(self, frame_id, test_frame_id, matching_algorithm, threshold_value, output_path):
        self.px_ref, self.px_cur = featureTracking(self.last_frame, self.new_frame, self.px_ref)
        if (frame_id == test_frame_id):
            featureMatching(self.last_frame, self.new_frame, matching_algorithm, threshold_value, output_path)
        E, mask = cv2.findEssentialMat(self.px_cur, self.px_ref, focal=self.focal, pp=self.pp, method=cv2.RANSAC, prob=0.999, threshold=1.0)
        _, R, t, mask = cv2.recoverPose(E, self.px_cur, self.px_ref, focal=self.focal, pp = self.pp)
        absolute_scale = self.getAbsoluteScale(frame_id)
        if(absolute_scale > 0.1):
            self.cur_t = self.cur_t + absolute_scale*self.cur_R.dot(t)
            self.cur_R = R.dot(self.cur_R)
        if(self.px_ref.shape[0] < kMinNumFeature):
            self.px_cur, self.desc = self.detector.detectAndCompute(self.new_frame, None)
            self.kps = self.px_cur
            self.px_cur = np.array([x.pt for x in self.px_cur], dtype=np.float32)
        self.px_ref = self.px_cur

    def update(self, img, frame_id, test_frame_id, matching_algorithm, threshold_value, output_path):
        assert(img.ndim==2 and img.shape[0]==self.cam.height and img.shape[1]==self.cam.width), "Frame: provided image has not the same size as the camera model or image is not grayscale"
        cv2.imshow('Road facing camera', img)
        self.new_frame = img
        if(self.frame_stage == STAGE_DEFAULT_FRAME):
            self.processFrame(frame_id, test_frame_id, matching_algorithm, threshold_value, output_path)
        elif(self.frame_stage == STAGE_SECOND_FRAME):
            self.processSecondFrame(test_frame_id, matching_algorithm, threshold_value, output_path)
        elif(self.frame_stage == STAGE_FIRST_FRAME):
            self.processFirstFrame()
        #img = cv2.drawKeypoints(img, self.kps, None, color=(0,0,255), flags=cv2.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS)
        self.last_frame = self.new_frame
