function stripTrailingSlash(value) {
    return value.replace(/\/+$/, '');
}

function getPreviewServiceFqdn() {
    return process.env.CUTOVER_PREVIEW_SERVICE_FQDN || process.env.SERVICE_FQDN || '';
}

function getCcdApiUrl(env) {
    if (process.env.CUTOVER_CCD_DATA_STORE_URL) {
        return stripTrailingSlash(process.env.CUTOVER_CCD_DATA_STORE_URL);
    }

    const previewServiceFqdn = getPreviewServiceFqdn();

    if (previewServiceFqdn) {
        return `https://ccd-data-store-api-${previewServiceFqdn}`;
    }

    return `http://ccd-data-store-api-${env}.service.core-compute-${env}.internal`;
}

function getS2sLeaseUrl(env) {
    const s2sBaseUrl = process.env.CUTOVER_S2S_URL || process.env.SERVICE_AUTH_PROVIDER_URL;

    if (s2sBaseUrl) {
        const normalisedS2sUrl = stripTrailingSlash(s2sBaseUrl);
        return normalisedS2sUrl.endsWith('/testing-support/lease')
            ? normalisedS2sUrl
            : `${normalisedS2sUrl}/testing-support/lease`;
    }

    return `http://rpe-service-auth-provider-${env}.service.core-compute-${env}.internal/testing-support/lease`;
}

module.exports = {
    getCcdApiUrl,
    getS2sLeaseUrl,
    getPreviewServiceFqdn
};
