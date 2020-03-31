#!/usr/bin/env bash

echo "Copying network to ../tmp/"
cp -a test-network/. tmp/
chmod a+x tmp/network.sh

. ./tmp/install_test.properties
if [[ ! -v _ORG_NAME_   || ! -v _FACT_CHECKERS_ORG_ ]]; then
    echo ERROR: You must define an _ORG_NAME_ and a _FACT_CHECKERS_ORG_ in install.properties 1>&2
    exit 1 # terminate and indicate error
fi

echo "Replacing organization names with provided configuration"
find ./tmp/ -type f \( -iname \*.yaml -o -iname \*.json -o -iname \*.sh \) -exec sed -i "s/_ORG_NAME_/$_ORG_NAME_/g" {} +
find ./tmp/ -type f \( -iname \*.yaml -o -iname \*.json -o -iname \*.sh \) -exec sed -i "s/_FACT_CHECKERS_ORG_/$_FACT_CHECKERS_ORG_/g" {} +

if [[ ! -v _ANOTHER_ORG_ ]]; then
    echo "Adding a third organization"
    find ./tmp/ -type f \( -iname \*.yaml -o -iname \*.json -o -iname \*.sh \) -exec sed -i "s/_ANOTHER_ORG_/$_ANOTHER_ORG_/g" {} +
fi

echo "Done preparing network"


# Example config for invoking the network as admin of org-1 (executed from the tmp/ directory)):

# export FABRIC_CFG_PATH=$PWD/../config
# export PATH=${PWD}/../bin:${PWD}:$PATH
# export CORE_PEER_TLS_ENABLED=true
# export CORE_PEER_LOCALMSPID="Org1MSP"
# export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org.nasa.com/peers/peer0.org.nasa.com/tls/ca.crt
# export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org.nasa.com/users/Admin@org.nasa.com/msp
# export CORE_PEER_ADDRESS=localhost:7051
