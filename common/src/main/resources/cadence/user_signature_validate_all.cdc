import Crypto

access(all) fun main(
  address: Address,
  signatures: [String],
  keyIndexes: [Int],
  message: String,
): Bool {
	let keyList = Crypto.KeyList()

	let account = getAccount(address)
	let keys = account.keys

	for keyIndex in keyIndexes {
		if let key = keys.get(keyIndex: keyIndex) {
			if key.isRevoked {
			    log("cannot verify: the key at this index is revoked")
				return false
			}
			keyList.add(
				PublicKey(
					publicKey: key.publicKey.publicKey,
					signatureAlgorithm: key.publicKey.signatureAlgorithm
				),
				hashAlgorithm: key.hashAlgorithm,
				weight: key.weight / 1000.0,
			)
		} else {
		    log("cannot verify: they key at this index doesn't exist")
			return false
		}
	}

	let signatureSet: [Crypto.KeyListSignature] = []

	var i = 0
	for signature in signatures {
	    log(signature)
	    log(i)
		signatureSet.append(
			Crypto.KeyListSignature(
				keyIndex: i,
				signature: signature.decodeHex()
			)
		)
		i = i + 1
	}

    log(message.utf8)

	return keyList.verify(
		signatureSet: signatureSet,
		signedData: message.utf8,
		domainSeparationTag: ""
	)
}