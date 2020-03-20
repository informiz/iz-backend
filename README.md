# The informiz.org Chaincode
## Chaincode For The informiz.org [Hyperledger Fabric](https://www.ibm.com/blockchain/hyperledger "IBM") Network
This repository contain the smart-contracts and data-types used by informiz.org. It defines transactions for managing 
fact-checkers, sources (e.g "The NASA official website"), hypothesises (factual claims) and texts (textual references).

### Setting up Hyperledger Fabric
You can use the IBM documentation to set up HF. 
First install the [prerequisites](https://hyperledger-fabric.readthedocs.io/en/latest/prereqs.html). Note that you will 
need docker and docker-compose, Go, Node.js and npm, and Python 2.7.
Next you can use the provided script in order to install the necessary binaries and images, as described in the 
[documentation](https://hyperledger-fabric.readthedocs.io/en/latest/install.html). You do this by executing the 
following command from the project's home directory:

`curl -sSL https://bit.ly/2ysbOFE | bash -s -- -s`

(The trailing `-s` excludes cloning the fabric-samples repository)

### Installing the application
#### Starting the network
 