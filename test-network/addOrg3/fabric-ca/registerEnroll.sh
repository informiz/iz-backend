

function createOrg3 {

  echo
	echo "Enroll the CA admin"
  echo
	mkdir -p ../organizations/peerOrganizations/_ANOTHER_ORG_/

	export FABRIC_CA_CLIENT_HOME=${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/
#  rm -rf $FABRIC_CA_CLIENT_HOME/fabric-ca-client-config.yaml
#  rm -rf $FABRIC_CA_CLIENT_HOME/msp

  set -x
  fabric-ca-client enroll -u https://admin:adminpw@localhost:11054 --caname ca-org3 --tls.certfiles ${PWD}/fabric-ca/org3/tls-cert.pem
  set +x

  echo 'NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/localhost-11054-ca-org3.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/localhost-11054-ca-org3.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/localhost-11054-ca-org3.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/localhost-11054-ca-org3.pem
    OrganizationalUnitIdentifier: orderer' > ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/msp/config.yaml

  echo
	echo "Register peer0"
  echo
  set -x
	fabric-ca-client register --caname ca-org3 --id.name peer0 --id.secret peer0pw --id.type peer --tls.certfiles ${PWD}/fabric-ca/org3/tls-cert.pem
  set +x

  echo
  echo "Register user"
  echo
  set -x
  fabric-ca-client register --caname ca-org3 --id.name user1 --id.secret user1pw --id.type client --tls.certfiles ${PWD}/fabric-ca/org3/tls-cert.pem
  set +x

  echo
  echo "Register the org admin"
  echo
  set -x
  fabric-ca-client register --caname ca-org3 --id.name org3admin --id.secret org3adminpw --id.type admin --tls.certfiles ${PWD}/fabric-ca/org3/tls-cert.pem
  set +x

	mkdir -p ../organizations/peerOrganizations/_ANOTHER_ORG_/peers
  mkdir -p ../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_

  echo
  echo "## Generate the peer0 msp"
  echo
  set -x
	fabric-ca-client enroll -u https://peer0:peer0pw@localhost:11054 --caname ca-org3 -M ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_/msp --csr.hosts peer0._ANOTHER_ORG_ --tls.certfiles ${PWD}/fabric-ca/org3/tls-cert.pem
  set +x

  cp ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/msp/config.yaml ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_/msp/config.yaml

  echo
  echo "## Generate the peer0-tls certificates"
  echo
  set -x
  fabric-ca-client enroll -u https://peer0:peer0pw@localhost:11054 --caname ca-org3 -M ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_/tls --enrollment.profile tls --csr.hosts peer0._ANOTHER_ORG_ --csr.hosts localhost --tls.certfiles ${PWD}/fabric-ca/org3/tls-cert.pem
  set +x


  cp ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_/tls/tlscacerts/* ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_/tls/ca.crt
  cp ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_/tls/signcerts/* ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_/tls/server.crt
  cp ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_/tls/keystore/* ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_/tls/server.key

  mkdir ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/msp/tlscacerts
  cp ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_/tls/tlscacerts/* ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/msp/tlscacerts/ca.crt

  mkdir ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/tlsca
  cp ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_/tls/tlscacerts/* ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/tlsca/tlsca._ANOTHER_ORG_-cert.pem

  mkdir ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/ca
  cp ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/peers/peer0._ANOTHER_ORG_/msp/cacerts/* ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/ca/ca._ANOTHER_ORG_-cert.pem

  mkdir -p ../organizations/peerOrganizations/_ANOTHER_ORG_/users
  mkdir -p ../organizations/peerOrganizations/_ANOTHER_ORG_/users/User1@_ANOTHER_ORG_

  echo
  echo "## Generate the user msp"
  echo
  set -x
	fabric-ca-client enroll -u https://user1:user1pw@localhost:11054 --caname ca-org3 -M ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/users/User1@_ANOTHER_ORG_/msp --tls.certfiles ${PWD}/fabric-ca/org3/tls-cert.pem
  set +x

  mkdir -p ../organizations/peerOrganizations/_ANOTHER_ORG_/users/Admin@_ANOTHER_ORG_

  echo
  echo "## Generate the org admin msp"
  echo
  set -x
	fabric-ca-client enroll -u https://org3admin:org3adminpw@localhost:11054 --caname ca-org3 -M ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/users/Admin@_ANOTHER_ORG_/msp --tls.certfiles ${PWD}/fabric-ca/org3/tls-cert.pem
  set +x

  cp ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/msp/config.yaml ${PWD}/../organizations/peerOrganizations/_ANOTHER_ORG_/users/Admin@_ANOTHER_ORG_/msp/config.yaml

}
