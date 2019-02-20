# You need to set this environmental variable to your AWS EC2 private key
# DO NOT SHARE THIS KEY WITH ANYONE UNLESS YOU WANT THEM USING YOUR AWS RESOURCES
if [ -z "$AWS_EC2_PRIVATE_KEY" ]; then 
	echo "error: enviromental variable AWS_EC2_PRIVATE_KEY isn't set";
	echo "it's the file with the .pem suffixes downloaded when you setup a EC2 instance on AWS"
	exit 1
else
	echo "using AWS_EC2_PRIVATE_KEY $AWS_EC2_PRIVATE_KEY";
fi

# You need to update this every time you re-launch the VM
if [ -z "VM_DNS" ]; then 
	echo "error: enviromental variable VM_DNS isn't set";
	echo "you can find the VM DNS address from the AWS EC2 web app as `Public DNS`"
	echo "the hostname should end with a suffic like `.compute.amazonaws.com`"
	exit 1
else
	echo "using VM_DNS $VM_DNS";
fi

# Define this once since we use it multiple times in ssh/scp scripts
SSH_HOST="ubuntu@$VM_DNS"
SCP_DIR="$SSH_HOST:~/nmt_python_scala_transpiler"

# This is the default port for Jupyter notebook. You can change it if you want
# but you'll need to specify the port when you launch the Jupyter notebook
# on the VM (within a screen session) using `jupyter notebook --port=XXXX`
JUPYTER_PORT=8888
