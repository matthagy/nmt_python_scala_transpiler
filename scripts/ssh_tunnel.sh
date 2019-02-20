# Create an SSH tunnel between remote AWS EC2 VM and our laptop
# Allows us to access the Jupyter notebook web interface on our laptop
# See https://aws.amazon.com/getting-started/tutorials/get-started-dlami/ for details
source `dirname $0`/config.sh
echo "creating SSH tunnel between our laptop and the EC2 AWS VM"
ssh \
	-i $AWS_EC2_PRIVATE_KEY \
	-L localhost:$JUPYTER_PORT:localhost:$JUPYTER_PORT \
	$SSH_HOST