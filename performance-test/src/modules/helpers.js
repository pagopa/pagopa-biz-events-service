
var eventIds = new Array();
var containerIds = new Array();
var fiscalCodeMap = {};
var cartMap = {};

export function randomString(length, charset) {
  let res = '';
  while (length--) res += charset[(Math.random() * charset.length) | 0];
  return res;
}

export function makeidMix(length) {
  var result           = '';
  var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  var charactersLength = characters.length;
  for ( var i = 0; i < length; i++ ) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength));
  }
  return result;
}

export function makeidNumber(length) {
  var result           = '';
  var characters       = '0123456789';
  var charactersLength = characters.length;
  for ( var i = 0; i < length; i++ ) {
    result += characters.charAt(Math.floor(Math.random() * 
charactersLength));
 }
 return result;
}

export function getRandomItemFromArray(items) {
	return items[Math.floor(Math.random()*items.length)];
}

export function makeRandomFiscalCode() {

  var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  var numbers = '0123456789';
  var charactersLength = characters.length;
  var numbersLength = numbers.length;

  var charValue = characters.charAt(Math.floor(Math.random() * charactersLength));
  var numValue = numbers.charAt(Math.floor(Math.random() * numbersLength));

  return charValue.repeat(6) + numValue.repeat(2) + charValue
  + numValue.repeat(2) + charValue + numValue.repeat(3) + charValue;

}

export async function createTxEvents() {
	// 1. setup code (once)
	// The setup code runs, setting up the test environment (optional) and generating data
	// used to reuse code for the same VU

	for (let i = 0; i < numberOfEventsToPreload; i++) {
		let id = makeidMix(25);
		let totalNotice = Math.floor(Math.random() * 3)
		let fiscalCode = makeRandomFiscalCode();
		for (let j = 0; j < totalNotice; j++) {
            var id_cart = totalNotice > 1 ? id+"_"+j : id;
            const response = await createTransactionListDocument(id, fiscalCode, totalNotice);
            console.log(response);
            check(response, { "status is 201": (res) => (res.statusCode === 201) });
            eventIds.push(id_cart);
		}
		containerIds.push(id);
        fiscalCodeMap[id] = fiscalCode;
        cartMap[id] = totalNotice>1;
	}

}

export function getTestData() {
    return { ids: containerIds, eventIds: eventIds, fiscalCodeMap: fiscalCodeMap , cartMap = cartMap }
}



