## Build Your First Network (BYFN)

< TODO: ********************************************* automate **************************************
< TODO: CAHNGE TO install.properties
cp -a test-network/. tmp/
chmod a+x tmp/network.sh

. ./tmp/install_test.properties
if [[ ! -v _ORG_NAME_   || ! -v _FACT_CHECKERS_ORG_ ]]; then
    echo ERROR: You must define an _ORG_NAME_ and a _FACT_CHECKERS_ORG_ in install.properties 1>&2
    exit 1 # terminate and indicate error
fi

find ./tmp/ -type f \( -iname \*.yaml -o -iname \*.json -o -iname \*.sh \) -exec sed -i "s/_ORG_NAME_/$_ORG_NAME_/g" {} +
find ./tmp/ -type f \( -iname \*.yaml -o -iname \*.json -o -iname \*.sh \) -exec sed -i "s/_FACT_CHECKERS_ORG_/$_FACT_CHECKERS_ORG_/g" {} +

if [[ ! -v _ANOTHER_ORG_ ]]; then
    find ./tmp/ -type f \( -iname \*.yaml -o -iname \*.json -o -iname \*.sh \) -exec sed -i "s/_ANOTHER_ORG_/$_ANOTHER_ORG_/g" {} +
fi

export FABRIC_CFG_PATH=$PWD/tmp/configtx/configtx.yaml

< TODO: query config
# _ORG_NAME_=org.nasa.com
# _FACT_CHECKERS_ORG_=factcheckers.informiz.org
export PATH=${PWD}/../bin:${PWD}:$PATH
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org.nasa.com/peers/peer0.org.nasa.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org.nasa.com/users/Admin@org.nasa.com/msp
export CORE_PEER_ADDRESS=localhost:7051

< TODO: ********************************************* automate **************************************


The directions for using this are documented in the Hyperledger Fabric
["Build Your First Network"](http://hyperledger-fabric.readthedocs.io/en/latest/build_network.html) tutorial.

*NOTE:* After navigating to the documentation, choose the documentation version that matches your version of Fabric



## Running the test network

You can use the `./network.sh` script to stand up a simple Fabric test network. The test network has two peer organizations with one peer each and a single node raft ordering service. You can also use the `./network.sh` script to create channels and deploy the informiz chaincode. For more information, see [Using the Fabric test network](https://hyperledger-fabric.readthedocs.io/en/latest/test_network.html). The test network is being introduced in Fabric v2.0 as the long term replacement for the `first-network` sample.

Before you can deploy the test network, you need to follow the instructions to [Install the Samples, Binaries and Docker Images](https://hyperledger-fabric.readthedocs.io/en/latest/install.html) in the Hyperledger Fabric documentation.
