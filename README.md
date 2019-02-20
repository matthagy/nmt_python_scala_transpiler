# Python to Scala Comprehension Transpiler using Neural Machine Translation
This project demonstrates how to convert simple Python expressions into Scala expressions. For example, consider the following Python expression.
```python
[a(x, z) for x in l if b(x, y)]
```
Within the context of this work, the equivalent Scala expressions is
```scala
l.filter((x) => b(x, y)).map((x) => a(x, z))
```

[Neural machine translation (NMT)](https://en.wikipedia.org/wiki/Neural_machine_translation)
is applied using a [recurrent neural network](https://en.wikipedia.org/wiki/Recurrent_neural_network)
to implement this transpiler. The RNN is created by adapting the excellent work of Zafarali Ahmed in
[keras-attention](https://github.com/datalogue/keras-attention),
which was originally developed to convert dates from varied human readable format to machine format.

If you'd like to reproduce this work, you'll want to use a machine with a GPU, because otherwise 
training the RNN will be too slow. This project includes scripts for interacting with an AWS EC2 VM
instance.

## Generating input data
The Python and Scala equivalent programs are generated programatically using a Scala program.
You can find the source code for this program in the src directory.

To build the Java jar you'll need to install sbt. You can then simply run
```bash
sbt assembly
```

Next, you can generate the expression data by running the script
```bash
mkdir data
bash scripts/push_expressions.sh 100000 > data/expressions
```

The number controls the number of programs generated and you can modify the accordingly.

## Working with an AWS EC2 VM
Start by creating a VM instance following instructions in
[Launch an AWS Deep Learning AMI](https://aws.amazon.com/getting-started/tutorials/get-started-dlami).

Next, you'll need to configure two environmental variables to specify how to interact
with your VM.

### Environmental variable AWS_EC2_PRIVATE_KEY
You need to specify the SSH PEM key that you configured for this VM. E.g.,
```bash
export AWS_EC2_PRIVATE_KEY=~/Downloads/key.pem
```

### Environmental variable VM_DNS
You need to specify the DNS address of the VM. You can find this on the EC2 control panel
for this VM instance.
```bash
export VM_DNS=NAME.REGION.compute.amazonaws.com
```

### Create an SSH tunnel to the VM
Connect the VM using SSH with the following script.
```bash
scripts/ssh_tunnel.sh
```
This also creates a port tunnel between the VM and your laptop so you can access the Jupyter
notebook instance from your laptop.

### Launch Jupyter notebook in a screen session
We want to launch the Jupyter notebook server on our VM. We want to run the Jupyter program
in a screen session so that if lose connection to our VM the Jupyter program will continue
to run.

You can create a screen session by running the following command in a shell.
```bash
screen -S jupyter
```
(The `-S` flag allows us to name this screen session so that we can easily re-attach to it.)

Within the screen session, launch the Jupyter notebook.
```bash
jupyter notebook
```

The Jupyter program will start and it will print out a long URL that looks like the following.
```
    Copy/paste this URL into your browser when you connect for the first time,
    to login with a token:
        http://localhost:8888/?token=876bc...
```

Follow the instructions and copy the URL into a browser on your laptop. You should see the Jupyter
main UI. From there you can open notebooks. (See instructions below for copying notebooks to the VM.)

In the VM terminal, you can dispatch from the screen session by pressing `Control` and `D` at the same
time. You'll want to keep the SSH session open to preserve the tunnel between the VM and you
laptop. If you lose SSH connection, you can always reconnect by re-running the command.
```bash
scripts/ssh_tunnel.sh
```

### Pushing expression data
Next, we want to copy the Python/Scala expressions from our laptop to the VM.
First, we'll create the necessary directories on the VM filesystem.
Within the SSH session, run the following command on the VM.
```bash
mkdir -p python_scala_comprehension_transpiler/data
```

Next, temporarily close the SSH session so we can run the command to copy data
without having to open a new terminal and reconfigure the environmental variables.
You can exit the SSH session by pressing `Control` and `D` together.

Run the following script on your laptop to copy data.
```bash
bash scripts/push_expressions.sh
```

### Pushing the notebook
Next, we'll want to copy the Jupyter notebooks from our laptop to the VM.
Run the following script to do so.
```bash
bash scripts/push_notebooks.sh
```

Once the upload finishes, you can resume your SSH session to recreate the SSH tunnel
so that you can access the Jupyter server on the VM from your laptop.
```bash
scripts/ssh_tunnel.sh
```

### Run the notebooks