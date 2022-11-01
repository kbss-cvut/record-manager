#!/bin/bash

PROJECT_DIR=`dirname "$0"`/..
cd $PROJECT_DIR

SFORMS_DIR=$PROJECT_DIR/../s-forms

SFORMS_VERSION=$(json -f $SFORMS_DIR/package.json "version")
SFORMS_NAME=$(json -f $SFORMS_DIR/package.json "name")
SFORMS_NAME_NOMALIZED=$(echo $SFORMS_NAME | tr -d '@' | tr '/' '-')


RM_PACKAGE_JSON_FILE_PATH=$PROJECT_DIR/src/main/webapp/package.json
SFORMS_DIST_FILE_PATH=$SFORMS_DIR/$SFORMS_NAME_NOMALIZED-$SFORMS_VERSION.tgz


echo "INFO: building $SFORMS_NAME:$SFORMS_VERSION ..." 
cd /home/blcha/projects/kbss/git/s-forms
rm -rf dist
mkdir -p dist
npm run build:lib



cd -
echo "INFO: Updating $RM_PACKAGE_JSON_FILE_PATH ..."
json -I -f $RM_PACKAGE_JSON_FILE_PATH -e 'this.dependencies["'$SFORMS_NAME'"]="'$SFORMS_DIST_FILE_PATH'"'


echo "INFO: Installing new dependency on $SFORMS_DIST_FILE_PATH ..."
rm -rf src/main/webapp/node_modules/$SFORMS_NAME
cd src/main/webapp
npm install


echo 'INFO: Done. Restart watchify of record-manager if needed !!!'
