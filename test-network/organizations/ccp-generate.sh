#!/bin/bash

function one_line_pem {
    echo "`awk 'NF {sub(/\\n/, ""); printf "%s\\\\\\\n",$0;}' $1`"
}

function json_ccp {
    local PP=$(one_line_pem $5)
    local CP=$(one_line_pem $6)
    sed -e "s/\${ORG_NUM}/$1/" \
        -e "s/\${ORG_NAME}/$2/" \
        -e "s/\${P0PORT}/$3/" \
        -e "s/\${CAPORT}/$4/" \
        -e "s#\${PEERPEM}#$PP#" \
        -e "s#\${CAPEM}#$CP#" \
        organizations/ccp-template.json
}

function yaml_ccp {
    local PP=$(one_line_pem $5)
    local CP=$(one_line_pem $6)
    sed -e "s/\${ORG_NUM}/$1/" \
        -e "s/\${ORG_NAME}/$2/" \
        -e "s/\${P0PORT}/$3/" \
        -e "s/\${CAPORT}/$4/" \
        -e "s#\${PEERPEM}#$PP#" \
        -e "s#\${CAPEM}#$CP#" \
        organizations/ccp-template.yaml | sed -e $'s/\\\\n/\\\n        /g'
}

ORG=1
P0PORT=7051
CAPORT=7054
PEERPEM=organizations/peerOrganizations/_ORG_NAME_/tlsca/tlsca._ORG_NAME_-cert.pem
CAPEM=organizations/peerOrganizations/_ORG_NAME_/ca/ca._ORG_NAME_-cert.pem

echo "$(json_ccp $ORG _ORG_NAME_ $P0PORT $CAPORT $PEERPEM $CAPEM)" > organizations/peerOrganizations/_ORG_NAME_/connection-org1.json
echo "$(yaml_ccp $ORG _ORG_NAME_ $P0PORT $CAPORT $PEERPEM $CAPEM)" > organizations/peerOrganizations/_ORG_NAME_/connection-org1.yaml

ORG=2
P0PORT=9051
CAPORT=8054
PEERPEM=organizations/peerOrganizations/_FACT_CHECKERS_ORG_/tlsca/tlsca._FACT_CHECKERS_ORG_-cert.pem
CAPEM=organizations/peerOrganizations/_FACT_CHECKERS_ORG_/ca/ca._FACT_CHECKERS_ORG_-cert.pem

echo "$(json_ccp $ORG _FACT_CHECKERS_ORG_ $P0PORT $CAPORT $PEERPEM $CAPEM)" > organizations/peerOrganizations/_FACT_CHECKERS_ORG_/connection-org2.json
echo "$(yaml_ccp $ORG _FACT_CHECKERS_ORG_ $P0PORT $CAPORT $PEERPEM $CAPEM)" > organizations/peerOrganizations/_FACT_CHECKERS_ORG_/connection-org2.yaml
