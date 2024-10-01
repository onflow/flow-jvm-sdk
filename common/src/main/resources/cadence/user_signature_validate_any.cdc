import Crypto

access(all) fun main(
  address: Address,
  signature: String,
  message: String
): Bool {
	let keyList = Crypto.KeyList()

	let account = getAccount(address)
	let keys = account.keys

	let signatureBytes = signature.decodeHex()
	let messageBytes = message.utf8

	var i = 0
	while true {
		if let key = keys.get(keyIndex: i) {
			if key.isRevoked {
				// do not check revoked keys
				i = i + 1
				continue
			}
			let pk = PublicKey(
					publicKey: key.publicKey.publicKey,
					signatureAlgorithm: key.publicKey.signatureAlgorithm
			)
			if pk.verify(
				signature: signatureBytes,
				signedData: messageBytes,
				domainSeparationTag: "",
				hashAlgorithm: key.hashAlgorithm
			) {
				// this key is good
				return true
			}
		} else {
			// checked all the keys, none of them match
			return false
		}
		i = i + 1
	}

	return false
}