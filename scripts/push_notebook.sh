echo "copying notebook from local directory to AWS EC2 VM"
source `dirname $0`/config.sh
scp -i $AWS_EC2_PRIVATE_KEY poc_rnn_polymer.ipynb $SCP_DIR/