# Python to Scala Transpiler using Neural Machine Translation
Matt Hagy, matthew.hagy@gmail.com

This project demonstrates how to convert simple Python expressions into Scala expressions. For example, consider the following Python expression.
```python
[a(x, z) for x in l if b(x, y)]
```
Within the context of this work, the equivalent Scala expressions is
```scala
l.filter((x) => b(x, y)).map((x) => a(x, z))
```

To learn more about this project, including a discussion of results, see the corresponding blog post,
[A Python to Scala transpiler using neural machine translation (NMT)](https://medium.com/@matthagy/a-python-to-scala-transpiler-using-neural-machine-translation-nmt-90d4d02afa70).

[Neural machine translation (NMT)](https://en.wikipedia.org/wiki/Neural_machine_translation)
is applied using a [recurrent neural network](https://en.wikipedia.org/wiki/Recurrent_neural_network) (RNN)
to implement this transpiler. The RNN is created by adapting the excellent work of Zafarali Ahmed in
[keras-attention](https://github.com/datalogue/keras-attention),
which was originally developed to convert dates from varied human readable format to machine format.

See Jupyter notebook
[python_scala_comprehension_transpiler.ipynb](https://github.com/matthagy/python_scala_comprehension_transpiler/blob/master/python_scala_comprehension_transpiler.ipynb)
for the results.
Note, you can directly view this file within the GitHub web app without cloning the
project and running Jupyter.

If you'd like to reproduce this work, you'll want to use a machine with a GPU, because otherwise 
training the RNN will be too slow. This project includes scripts for interacting with an AWS EC2 VM
instance.

## Generating input data
The Python and Scala equivalent programs are generated programatically using a Scala program.
You can find the source code for this program in the src directory.

To build the Java jar you'll need to install [sbt](https://www.scala-sbt.org).
With [Homebrew](https://brew.sh), you can install sbt using the following command.
```bash
brew install sbt
```

You can then simply run the following command in the project root directory.
```bash
sbt assembly
```

Next, you can generate the expression data by running these commands.
```bash
mkdir data
bash scripts/push_expressions.sh 400000 > data/expressions
```
(Note, this was the largest data I could process on a 30 GB VM in the subsequent training step.)

The number controls the number of expressions generated and you can modify it accordingly.

## Working with an AWS EC2 VM
Start by creating a VM instance following instructions in
[Launch an AWS Deep Learning AMI](https://aws.amazon.com/getting-started/tutorials/get-started-dlami).
Select a VM that includes a GPU.
I used a [g3s.xlarge](https://aws.amazon.com/ec2/instance-types/g3/) instance. 

Next, you'll need to configure two environmental variables to specify how to interact
with your VM using scripts in this project.

### Environmental variable AWS_EC2_PRIVATE_KEY
You need to specify the SSH PEM key that you configured for this VM. E.g.,
```bash
export AWS_EC2_PRIVATE_KEY=~/Downloads/key.pem
```

### Environmental variable VM_DNS
You need to specify the DNS address of the VM. You can find this on the EC2 control panel
for this VM instance under the attribute "Public DNS".
```bash
export VM_DNS=NAME.REGION.compute.amazonaws.com
```

### Create an SSH tunnel to the VM
Connect to the VM using SSH with the following script.
```bash
bash scripts/ssh_tunnel.sh
```
This also creates a port tunnel between the VM and your laptop so you can access the Jupyter
notebook instance from your laptop.

### Launch Jupyter notebook in a screen session
Next, we want to launch the Jupyter notebook server on our VM. We run the Jupyter program
in a screen session so that if lose our SSH connection to the VM the Jupyter program
will continue to run.

You can create a screen session by running the following command in a shell.
```bash
screen -S jupyter
```
(The `-S` flag allows us to name this screen session so that we can easily re-attach to it later.)

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
laptop. If you lose SSH connection, you can always reconnect by re-running the script `scripts/ssh_tunnel.sh`.

### Pushing expression data
Next, we want to copy the Python/Scala expressions from our laptop to the VM.
First, we'll create the necessary directories on the VM filesystem.
Within the SSH session, run the following command on the VM.
```bash
mkdir -p nmt_python_scala_transpiler/data
```

Next, temporarily close the SSH session so we can run the command to copy data
without having to open a new terminal and reconfigure the environmental variables.
You can exit the SSH session by pressing `Control` and `D` together.

Run the following script on your laptop to copy data.
```bash
bash scripts/push_expressions.sh
```

### Pushing the notebook
Next, we'll want to copy the Jupyter notebook from our laptop to the VM.
Run the following script to do so.
```bash
bash scripts/push_notebook.sh
```

Once the upload finishes, you can resume your SSH session to recreate the SSH tunnel
so that you can access the Jupyter server on the VM from your laptop.
```bash
bash scripts/ssh_tunnel.sh
```

### Run the notebook
You can now open the notebook on the Jupyter webapp running on your laptop.
Open the file named `nmt_python_scala_transpiler.ipynb`.
You can run all cells in this notebook to repeat this work.
The training cell is an infinite loop and you can interrupt it when you're
satisfied with level of training.

I allowed the notebook to run over night using a GPU on EC2.

### Copy results back
Lastly, you can copy the modified Jupyter notebook back to your laptop by running
the following script on your laptop.
```bash
bash scripts/pull_notebook.sh
```

## Closing Remarks
Thanks for checking out this project! I welcome any revisions or extensions to this work
so feel free to submit a PR.
