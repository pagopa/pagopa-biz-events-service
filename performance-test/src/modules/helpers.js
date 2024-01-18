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



