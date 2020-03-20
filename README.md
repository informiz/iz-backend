# The informiz.org Chaincode
## Chaincode For The informiz.org [Hyperledger Fabric](https://www.ibm.com/blockchain/hyperledger "IBM") Network
This repository contain the smart-contracts and data-types used by informiz.org. It defines transactions for managing 
fact-checkers, sources (e.g "The NASA official website"), hypothesises (factual claims) and texts (textual references).

You can deploy this application on an existing Hyperledger Fabric network. To set up the network please follow the 
[Hyperledger Fabric documentation](https://hyperledger-fabric.readthedocs.io/en/release-1.4/getting_started.html). 
Note that you will need docker and docker-compose, Go, Node.js and npm, and Python 2.7. The documentation provides an 
installation script which will also download the Hyperledger Fabric samples, including convenient tools and scripts 
for starting and stopping the network.

Check in which directory the installation has cloned the `fabric-samples` repository.

### Installing the application
#### Starting the network
 