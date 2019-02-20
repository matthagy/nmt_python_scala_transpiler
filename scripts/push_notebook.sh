echo "copying notebook from local directory to AWS EC2 VM"
source `dirname $0`/config.sh
scp -i $AWS_EC2_PRIVATE_KEY nmt_python_scala_transpiler.ipynb $SCP_DIR/