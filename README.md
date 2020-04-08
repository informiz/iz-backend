# The informiz.org Chaincode
## Chaincode For The informiz.org [Hyperledger Fabric](https://www.ibm.com/blockchain/hyperledger "IBM HLF") Network

This repository contains the smart-contracts and data-types used by informiz.org. It defines transactions for managing 
fact-checkers, sources (e.g "The NASA official website"), hypothesises (factual claims) and texts (textual references).

### Setting up a Hyperledger Fabric network

Follow The instructions in the `test-network/README.md` file to set up a network and install the chaincode.

### Using the network
Set the following environment variables for invoking the network as e.g admin of org-1 (note that the paths are relative 
to the created `<project home dir>/tmp/` directory)):

export FABRIC_CFG_PATH=$PWD/../config
export PATH=${PWD}/../bin:${PWD}:$PATH
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org.nasa.com/peers/peer0.org.nasa.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org.nasa.com/users/Admin@org.nasa.com/msp
export CORE_PEER_ADDRESS=localhost:7051

You can then execute HLF commands, e.g:

`peer chaincode query -C mychannel -n informiz -c '{"Args":["queryAllSources", "50", ""]}'`.



 