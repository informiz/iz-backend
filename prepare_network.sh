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
