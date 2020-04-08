## Build Your First Network (BYFN)

This automated setup process is tweaked from the Hyperledger Fabric
["Build Your First Network"](http://hyperledger-fabric.readthedocs.io/en/latest/build_network.html) tutorial.

*NOTE:* After navigating to the documentation, choose the documentation version that matches your version of Fabric

## Install Hyperledger Fabric

Before you can deploy the test network, you need to [Install the Samples, Binaries and Docker Images](https://hyperledger-fabric.readthedocs.io/en/latest/install.html) of HLF.
You can do this by executing:

`curl -sSL https://bit.ly/2ysbOFE | bash -s -- -s`

from the project's home directory (the trailing `-s` skips unnecessary cloning of the fabric-samples repository).

## Prepare the temporary network folder

* In the `test-network/install_test.properties`, change the `_ORG_NAME_` value to your own organization's name.
* Run the `prepare_network.sh` script by executing `./prepare_network.sh` from the `test-network/` directory.

## Running the test network

You can use the `./network.sh` script to spin up a simple Fabric test network. The test network has two peer organizations with one peer each and a single node raft ordering service. You can also use the `./network.sh` script to create channels and deploy the informiz chaincode. For more information, see [Using the Fabric test network](https://hyperledger-fabric.readthedocs.io/en/latest/test_network.html).

Change directory to the `<project home dir>/tmp/` folder created in the previous step.
* Start the network by executing `./network.sh up`.
* Create a channel with `./network.sh createChannel`.
* Deploy the informiz chaincode by executing `./network.sh deployCC`.
