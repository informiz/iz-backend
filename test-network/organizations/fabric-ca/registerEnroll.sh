

function createOrg1 {

  echo
	echo "Enroll the CA admin"
  echo
	mkdir -p organizations/peerOrganizations/_ORG_NAME_/

	export FABRIC_CA_CLIENT_HOME=${PWD}/organizations/peerOrganizations/_ORG_NAME_/
#  rm -rf $FABRIC_CA_CLIENT_HOME/fabric-ca-client-config.yaml
#  rm -rf $FABRIC_CA_CLIENT_HOME/msp

  set -x
  fabric-ca-client enroll -u https://admin:adminpw@localhost:7054 --caname ca-org1 --tls.certfiles ${PWD}/organizations/fabric-ca/org1/tls-cert.pem
  set +x

  echo 'NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/localhost-7054-ca-org1.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/localhost-7054-ca-org1.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/localhost-7054-ca-org1.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/localhost-7054-ca-org1.pem
    OrganizationalUnitIdentifier: orderer' > ${PWD}/organizations/peerOrganizations/_ORG_NAME_/msp/config.yaml

  echo
	echo "Register peer0"
  echo
  set -x
	fabric-ca-client register --caname ca-org1 --id.name peer0 --id.secret peer0pw --id.type peer --tls.certfiles ${PWD}/organizations/fabric-ca/org1/tls-cert.pem
  set +x

  echo
  echo "Register user"
  echo
  set -x
  fabric-ca-client register --caname ca-org1 --id.name user1 --id.secret user1pw --id.type client --tls.certfiles ${PWD}/organizations/fabric-ca/org1/tls-cert.pem
  set +x

  echo
  echo "Register the org admin"
  echo
  set -x
  fabric-ca-client register --caname ca-org1 --id.name org1admin --id.secret org1adminpw --id.type admin --tls.certfiles ${PWD}/organizations/fabric-ca/org1/tls-cert.pem
  set +x

	mkdir -p organizations/peerOrganizations/_ORG_NAME_/peers
  mkdir -p organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_

  echo
  echo "## Generate the peer0 msp"
  echo
  set -x
	fabric-ca-client enroll -u https://peer0:peer0pw@localhost:7054 --caname ca-org1 -M ${PWD}/organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_/msp --csr.hosts peer0._ORG_NAME_ --tls.certfiles ${PWD}/organizations/fabric-ca/org1/tls-cert.pem
  set +x

  cp ${PWD}/organizations/peerOrganizations/_ORG_NAME_/msp/config.yaml ${PWD}/organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_/msp/config.yaml

  echo
  echo "## Generate the peer0-tls certificates"
  echo
  set -x
  fabric-ca-client enroll -u https://peer0:peer0pw@localhost:7054 --caname ca-org1 -M ${PWD}/organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_/tls --enrollment.profile tls --csr.hosts peer0._ORG_NAME_ --csr.hosts localhost --tls.certfiles ${PWD}/organizations/fabric-ca/org1/tls-cert.pem
  set +x


  cp ${PWD}/organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_/tls/tlscacerts/* ${PWD}/organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_/tls/ca.crt
  cp ${PWD}/organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_/tls/signcerts/* ${PWD}/organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_/tls/server.crt
  cp ${PWD}/organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_/tls/keystore/* ${PWD}/organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_/tls/server.key

  mkdir ${PWD}/organizations/peerOrganizations/_ORG_NAME_/msp/tlscacerts
  cp ${PWD}/organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_/tls/tlscacerts/* ${PWD}/organizations/peerOrganizations/_ORG_NAME_/msp/tlscacerts/ca.crt

  mkdir ${PWD}/organizations/peerOrganizations/_ORG_NAME_/tlsca
  cp ${PWD}/organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_/tls/tlscacerts/* ${PWD}/organizations/peerOrganizations/_ORG_NAME_/tlsca/tlsca._ORG_NAME_-cert.pem

  mkdir ${PWD}/organizations/peerOrganizations/_ORG_NAME_/ca
  cp ${PWD}/organizations/peerOrganizations/_ORG_NAME_/peers/peer0._ORG_NAME_/msp/cacerts/* ${PWD}/organizations/peerOrganizations/_ORG_NAME_/ca/ca._ORG_NAME_-cert.pem

  mkdir -p organizations/peerOrganizations/_ORG_NAME_/users
  mkdir -p organizations/peerOrganizations/_ORG_NAME_/users/User1@_ORG_NAME_

  echo
  echo "## Generate the user msp"
  echo
  set -x
	fabric-ca-client enroll -u https://user1:user1pw@localhost:7054 --caname ca-org1 -M ${PWD}/organizations/peerOrganizations/_ORG_NAME_/users/User1@_ORG_NAME_/msp --tls.certfiles ${PWD}/organizations/fabric-ca/org1/tls-cert.pem
  set +x

  mkdir -p organizations/peerOrganizations/_ORG_NAME_/users/Admin@_ORG_NAME_

  echo
  echo "## Generate the org admin msp"
  echo
  set -x
	fabric-ca-client enroll -u https://org1admin:org1adminpw@localhost:7054 --caname ca-org1 -M ${PWD}/organizations/peerOrganizations/_ORG_NAME_/users/Admin@_ORG_NAME_/msp --tls.certfiles ${PWD}/organizations/fabric-ca/org1/tls-cert.pem
  set +x

  cp ${PWD}/organizations/peerOrganizations/_ORG_NAME_/msp/config.yaml ${PWD}/organizations/peerOrganizations/_ORG_NAME_/users/Admin@_ORG_NAME_/msp/config.yaml

}


function createOrg2 {

  echo
	echo "Enroll the CA admin"
  echo
	mkdir -p organizations/peerOrganizations/_FACT_CHECKERS_ORG_/

	export FABRIC_CA_CLIENT_HOME=${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/
#  rm -rf $FABRIC_CA_CLIENT_HOME/fabric-ca-client-config.yaml
#  rm -rf $FABRIC_CA_CLIENT_HOME/msp

  set -x
  fabric-ca-client enroll -u https://admin:adminpw@localhost:8054 --caname ca-org2 --tls.certfiles ${PWD}/organizations/fabric-ca/org2/tls-cert.pem
  set +x

  echo 'NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/localhost-8054-ca-org2.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/localhost-8054-ca-org2.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/localhost-8054-ca-org2.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/localhost-8054-ca-org2.pem
    OrganizationalUnitIdentifier: orderer' > ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/msp/config.yaml

  echo
	echo "Register peer0"
  echo
  set -x
	fabric-ca-client register --caname ca-org2 --id.name peer0 --id.secret peer0pw --id.type peer --tls.certfiles ${PWD}/organizations/fabric-ca/org2/tls-cert.pem
  set +x

  echo
  echo "Register user"
  echo
  set -x
  fabric-ca-client register --caname ca-org2 --id.name user1 --id.secret user1pw --id.type client --tls.certfiles ${PWD}/organizations/fabric-ca/org2/tls-cert.pem
  set +x

  echo
  echo "Register the org admin"
  echo
  set -x
  fabric-ca-client register --caname ca-org2 --id.name org2admin --id.secret org2adminpw --id.type admin --tls.certfiles ${PWD}/organizations/fabric-ca/org2/tls-cert.pem
  set +x

	mkdir -p organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers
  mkdir -p organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_

  echo
  echo "## Generate the peer0 msp"
  echo
  set -x
	fabric-ca-client enroll -u https://peer0:peer0pw@localhost:8054 --caname ca-org2 -M ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_/msp --csr.hosts peer0._FACT_CHECKERS_ORG_ --tls.certfiles ${PWD}/organizations/fabric-ca/org2/tls-cert.pem
  set +x

  cp ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/msp/config.yaml ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_/msp/config.yaml

  echo
  echo "## Generate the peer0-tls certificates"
  echo
  set -x
  fabric-ca-client enroll -u https://peer0:peer0pw@localhost:8054 --caname ca-org2 -M ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_/tls --enrollment.profile tls --csr.hosts peer0._FACT_CHECKERS_ORG_ --csr.hosts localhost --tls.certfiles ${PWD}/organizations/fabric-ca/org2/tls-cert.pem
  set +x


  cp ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_/tls/tlscacerts/* ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_/tls/ca.crt
  cp ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_/tls/signcerts/* ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_/tls/server.crt
  cp ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_/tls/keystore/* ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_/tls/server.key

  mkdir ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/msp/tlscacerts
  cp ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_/tls/tlscacerts/* ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/msp/tlscacerts/ca.crt

  mkdir ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/tlsca
  cp ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_/tls/tlscacerts/* ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/tlsca/tlsca._FACT_CHECKERS_ORG_-cert.pem

  mkdir ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/ca
  cp ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/peers/peer0._FACT_CHECKERS_ORG_/msp/cacerts/* ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/ca/ca._FACT_CHECKERS_ORG_-cert.pem

  mkdir -p organizations/peerOrganizations/_FACT_CHECKERS_ORG_/users
  mkdir -p organizations/peerOrganizations/_FACT_CHECKERS_ORG_/users/User1@_FACT_CHECKERS_ORG_

  echo
  echo "## Generate the user msp"
  echo
  set -x
	fabric-ca-client enroll -u https://user1:user1pw@localhost:8054 --caname ca-org2 -M ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/users/User1@_FACT_CHECKERS_ORG_/msp --tls.certfiles ${PWD}/organizations/fabric-ca/org2/tls-cert.pem
  set +x

  mkdir -p organizations/peerOrganizations/_FACT_CHECKERS_ORG_/users/Admin@_FACT_CHECKERS_ORG_

  echo
  echo "## Generate the org admin msp"
  echo
  set -x
	fabric-ca-client enroll -u https://org2admin:org2adminpw@localhost:8054 --caname ca-org2 -M ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/users/Admin@_FACT_CHECKERS_ORG_/msp --tls.certfiles ${PWD}/organizations/fabric-ca/org2/tls-cert.pem
  set +x

  cp ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/msp/config.yaml ${PWD}/organizations/peerOrganizations/_FACT_CHECKERS_ORG_/users/Admin@_FACT_CHECKERS_ORG_/msp/config.yaml

}

function createOrderer {

  echo
	echo "Enroll the CA admin"
  echo
	mkdir -p organizations/ordererOrganizations/informiz.org

	export FABRIC_CA_CLIENT_HOME=${PWD}/organizations/ordererOrganizations/informiz.org
#  rm -rf $FABRIC_CA_CLIENT_HOME/fabric-ca-client-config.yaml
#  rm -rf $FABRIC_CA_CLIENT_HOME/msp

  set -x
  fabric-ca-client enroll -u https://admin:adminpw@localhost:9054 --caname ca-orderer --tls.certfiles ${PWD}/organizations/fabric-ca/ordererOrg/tls-cert.pem
  set +x

  echo 'NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/localhost-9054-ca-orderer.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/localhost-9054-ca-orderer.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/localhost-9054-ca-orderer.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/localhost-9054-ca-orderer.pem
    OrganizationalUnitIdentifier: orderer' > ${PWD}/organizations/ordererOrganizations/informiz.org/msp/config.yaml


  echo
	echo "Register orderer"
  echo
  set -x
	fabric-ca-client register --caname ca-orderer --id.name orderer --id.secret ordererpw --id.type orderer --tls.certfiles ${PWD}/organizations/fabric-ca/ordererOrg/tls-cert.pem
    set +x

  echo
  echo "Register the orderer admin"
  echo
  set -x
  fabric-ca-client register --caname ca-orderer --id.name ordererAdmin --id.secret ordererAdminpw --id.type admin --tls.certfiles ${PWD}/organizations/fabric-ca/ordererOrg/tls-cert.pem
  set +x

	mkdir -p organizations/ordererOrganizations/informiz.org/orderers
  mkdir -p organizations/ordererOrganizations/informiz.org/orderers/informiz.org

  mkdir -p organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org

  echo
  echo "## Generate the orderer msp"
  echo
  set -x
	fabric-ca-client enroll -u https://orderer:ordererpw@localhost:9054 --caname ca-orderer -M ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/msp --csr.hosts orderer.informiz.org --csr.hosts localhost --tls.certfiles ${PWD}/organizations/fabric-ca/ordererOrg/tls-cert.pem
  set +x

  cp ${PWD}/organizations/ordererOrganizations/informiz.org/msp/config.yaml ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/msp/config.yaml

  echo
  echo "## Generate the orderer-tls certificates"
  echo
  set -x
  fabric-ca-client enroll -u https://orderer:ordererpw@localhost:9054 --caname ca-orderer -M ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/tls --enrollment.profile tls --csr.hosts orderer.informiz.org --csr.hosts localhost --tls.certfiles ${PWD}/organizations/fabric-ca/ordererOrg/tls-cert.pem
  set +x

  cp ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/tls/tlscacerts/* ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/tls/ca.crt
  cp ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/tls/signcerts/* ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/tls/server.crt
  cp ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/tls/keystore/* ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/tls/server.key

  mkdir ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/msp/tlscacerts
  cp ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/tls/tlscacerts/* ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/msp/tlscacerts/tlsca.informiz.org-cert.pem

  mkdir ${PWD}/organizations/ordererOrganizations/informiz.org/msp/tlscacerts
  cp ${PWD}/organizations/ordererOrganizations/informiz.org/orderers/orderer.informiz.org/tls/tlscacerts/* ${PWD}/organizations/ordererOrganizations/informiz.org/msp/tlscacerts/tlsca.informiz.org-cert.pem

  mkdir -p organizations/ordererOrganizations/informiz.org/users
  mkdir -p organizations/ordererOrganizations/informiz.org/users/Admin@informiz.org

  echo
  echo "## Generate the admin msp"
  echo
  set -x
	fabric-ca-client enroll -u https://ordererAdmin:ordererAdminpw@localhost:9054 --caname ca-orderer -M ${PWD}/organizations/ordererOrganizations/informiz.org/users/Admin@informiz.org/msp --tls.certfiles ${PWD}/organizations/fabric-ca/ordererOrg/tls-cert.pem
  set +x

  cp ${PWD}/organizations/ordererOrganizations/informiz.org/msp/config.yaml ${PWD}/organizations/ordererOrganizations/informiz.org/users/Admin@informiz.org/msp/config.yaml


}
