const { put } = require('./common');

async function createToken(fiscalCode) {
    const tokenizer_url = process.env.TOKENIZER_URL;
    const token_api_key = process.env.TOKENIZER_API_KEY;
    const headers = {
        "x-api-key": token_api_key
    };

    const response = await put(tokenizer_url, { "pii": fiscalCode }, { headers })
    
    return response.data;
}

module.exports = {
    createToken
}
