#!/bin/bash
export WORKSPACE="${PWD}"
export TMP_PATH="$(cygpath -u "${WORKSPACE}/tmp")"
export RELEASE_PATH="$(cygpath -u "${WORKSPACE}/release")"
export ARCHIVE_NAME_WITH_PATH="$(find . -name "icedtea-web-*.win.bin.zip")"
export ARCHIVE_NAME="$(basename "${ARCHIVE_NAME_WITH_PATH}")"
export MSI_NAME_WITH_PATH="$(find . -name "icedtea-web-*.msi")"
export MSI_NAME="$(basename "${MSI_NAME_WITH_PATH}")"

mkdir ${TMP_PATH}
mkdir ${RELEASE_PATH}

unzip -q ${ARCHIVE_NAME} -d ${TMP_PATH}
cd ${TMP_PATH}

echo "Signing Binary"
find . -type f -name '*.exe' > sign.txt

/cygdrive/c/AzureSignTool/AzureSignTool.exe sign --file-digest sha256 --description-url "$description_url" \
--no-page-hashing --timestamp-rfc3161 http://timestamp.digicert.com --timestamp-digest sha256 \
--azure-key-vault-url "$key_vault_url" --azure-key-vault-client-id "$key_vault_client_id" \
--azure-key-vault-client-secret "$key_vault_client_secret" --azure-key-vault-certificate "$key_vault_certificate" \
--input-file-list sign.txt

zip -rq ${ARCHIVE_NAME} *
cp ${ARCHIVE_NAME} ${RELEASE_PATH}/${ARCHIVE_NAME}

echo "Signing Installer"
cp ${WORKSPACE}/win-installer.build/${MSI_NAME} ${TMP_PATH}/${MSI_NAME}

/cygdrive/c/AzureSignTool/AzureSignTool.exe sign "${MSI_NAME}" --file-digest sha256 \
--description-url "$description_url" --no-page-hashing --timestamp-rfc3161 http://timestamp.digicert.com \
--timestamp-digest sha256 --azure-key-vault-url "$key_vault_url" --azure-key-vault-client-id "$key_vault_client_id" \
--azure-key-vault-client-secret "$key_vault_client_secret" --azure-key-vault-certificate "$key_vault_certificate"

cp ${TMP_PATH}/${MSI_NAME} ${RELEASE_PATH}/${MSI_NAME}

ls ${RELEASE_PATH}

cd $RELEASE_PATH

for zip in icedtea-web-*; do
	shasum -a 256 "$zip" > "$zip.sha256.txt"
done