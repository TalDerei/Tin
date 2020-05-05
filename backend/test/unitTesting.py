#!/usr/bin/python3
import sys, os, ast, json, time
import subprocess as sp

# Unit testing for ...
# 1) user login and registration
# 2) messsage post
# 3) message edit & delete
# 4) slang filtering
# 5) Like vote test (upvote, delete)
# 6) file upload and download
# 7) append file & hyperlink to correspond message

#
# reference to check other functions
#
def get_messages(): # show all messages
    reference = None
    args = ['bash','get.sh']
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    stdout, stderr = result.communicate()
    out = stdout.decode('utf-8')
    reference = json.loads(out)
    return reference

def get_files():     # show all files
    reference = None
    args = ['bash','getFiles.sh']
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    stdout, stderr = result.communicate()
    out = stdout.decode('utf-8')
    reference = json.loads(out)
    return reference

def get_join():     # show all informations such as flag, file, link, and user for each message
    reference = None
    args = ['bash','getJoin.sh']
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    stdout, stderr = result.communicate()
    out = stdout.decode('utf-8')
    reference = json.loads(out)
    return reference




# 
# 0) validate Google ID Token at https://oauth2.googleapis.com/tokeninfo before testing
# 
def validate_token():
    args = ['bash','valid.sh']
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    stdout, stderr = result.communicate()
    out = stdout.decode('utf-8')
    output = ast.literal_eval(out)
    assert(output['email_verified'] == 'true')
    # write textfile for testing
    googleID = output['sub']
    f = open('userID.dat', 'w')
    f.write(googleID)
    f.close()
    print('pass step0; validation of Google ID Token')

# 
# 1) user login 
# 
def test_login():
    args = ['bash','postUser.sh']
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    stdout, stderr = result.communicate()
    out = stdout.decode('utf-8')
    output = ast.literal_eval(out)
    googleID = open('userID.dat').read()
    assert(output['mUser_id'] == googleID)
    print('pass step1; success registration')

# 
# 2) message posting
# 
def test_message_posting():
    target_message = "shouldBeThis"
    args = ['bash','post.sh', target_message]
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    stdout, stderr = result.communicate()
    out = stdout.decode('utf-8')
    output = json.loads(out)
    # get messageID from POST response
    lastID = output['mMessage']
    # check with [GET]/messages
    time.sleep(1)
    reference = get_messages()
    for mData in reference['mData']:
        if int(mData['mId']) == int(lastID):
            message = mData['mMessage']
    assert(message == target_message)
    print('pass step2; success message posting')

# 
# 3) message edit 
# 
def test_message_edit():
    target_message_id = 1        
    target_message = "put change!" # 1st message should be "put change!"
    args = ['bash','put.sh']
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    time.sleep(1)
    # check with [GET]/messages
    reference = get_messages()
    for mData in reference['mData']:
        if int(mData['mId']) == 1:
            message = mData['mMessage']
    assert(message == target_message)
    print('pass step3; done to edit message')


# 
# 4) slang Filtering
# 

def test_message_slang():
    target_message = "b****! f***!" # should be displayed like this.
    args = ['bash','slang.sh']      # actual post is "bitch! fuck!"
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    stdout, stderr = result.communicate()
    out = stdout.decode('utf-8')
    output = json.loads(out)
    # get messageID from POST response
    lastID = output['mMessage']
    # check with [GET]/messages
    reference = get_join()
    for mData in reference['mData']:
        if int(mData['mId']) == int(lastID):
            flag = mData['flag']
            message = mData['mMessage']
    assert(flag == True)
    assert(message == target_message)
    print('pass step4; filtering bad word and flag is up!')

# 
# 5) Like vote test (upvote, delete)
# 
def test_like_vote():
    target_message_id = 1        # 1st message should have 1 vote!
    args = ['bash','putLike.sh'] # put Like vote on 1st message.
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    # check with [GET]/messages
    time.sleep(1)
    reference = get_messages()
    targetMessage = list(filter(lambda x: int(x['mId']) == 1, reference['mData'])).pop()
    assert(targetMessage['mLikes'] == 1)
    print('pass step5; success Like vote!')

def test_like_vote_dupl():
    args = ['bash','putLike.sh'] # put Like vote second time! it should not be increased.
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    # check with [GET]/messages
    time.sleep(1)
    reference = get_messages()
    targetMessage = list(filter(lambda x: int(x['mId']) == 1, reference['mData'])).pop()
    assert(targetMessage['mLikes'] == 1)
    print('pass step5; Like vote cannot be cumulative!')

def test_like_vote_delete():
    args = ['bash','delLike.sh'] # delete Like vote on 1st message.
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    time.sleep(1)
    reference = get_messages()
    targetMessage = list(filter(lambda x: int(x['mId']) == 1, reference['mData'])).pop()
    assert(targetMessage['mLikes'] == 0)
    print('pass step5; can delete Like vote')

# 
# 6) delete message
# 
def test_delete_message():
    reference = get_messages()
    maxId = max(list(map(lambda x: int(x['mId']), reference['mData'])))
    args = ['bash','delete.sh', str(maxId)] # last mID should be removed!
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    time.sleep(1)
    reference = get_messages()
    has_message = False
    for mData in reference['mData']:
        if mData['mId'] == maxId:
            has_message = True
    assert(has_message  == False)
    print('pass step6; done to remove message!')

# 
# 7) file upload
# 
def test_file_upload():
    # message posting
    target_message = "forTestingFileUpload"
    args = ['bash','post.sh', target_message]
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    stdout, stderr = result.communicate()
    out = stdout.decode('utf-8')
    output = json.loads(out)
    # get messageID from POST response
    lastID = output['mMessage']

    args = ['bash','postFile.sh'] # image upload
    result = sp.Popen(args, stderr=sp.STDOUT, stdout=sp.PIPE, close_fds=True)
    stdout, stderr = result.communicate()
    out = stdout.decode('utf-8')
    output = json.loads(out)
    fileID = output['mData']
    # return fileID as a response
    f = open('fileID.dat', 'w')
    f.write(fileID)
    f.close()
    time.sleep(1)

    # file check
    targetFile = {'mime':'', "fname":'', 'size':0}
    file_refer = get_files()
    for f in file_refer['mData']:
        if f['fileid'] == fileID:
            targetFile = f
    assert(targetFile['mime'] == "application/png")
    assert(targetFile['fname'] == "protein.png")
    assert(targetFile['size'] == 29110)

    # file should be linked to last post message
    reference = get_join()
    for mData in reference['mData']:
        if mData['mId'] == lastID:
            assert(mData['fileID'] == fileID)
    print('pass step7; success on file upload!')

    


if __name__ == '__main__':
    validate_token()
    test_login()
    test_message_posting()
    test_message_edit()
    test_message_slang()
    test_like_vote()
    test_like_vote_dupl()
    test_like_vote_delete()
    test_delete_message()
    test_file_upload()
