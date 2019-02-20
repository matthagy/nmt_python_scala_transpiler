echo "copying expressions from local directory to AWS EC2 VM"
source `dirname $0`/config.sh
scp -i $AWS_EC2_PRIVATE_KEY data/expressions $SCP_DIR/data/