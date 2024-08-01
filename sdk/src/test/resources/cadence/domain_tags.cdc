import Crypto

access(all) fun main(
  rawPublicKeys: [String],
  weights: [UFix64],
  signatures: [String],
  message: String,
): Bool {

  var i = 0
  let keyList = Crypto.KeyList()
  for rawPublicKey in rawPublicKeys {
    keyList.add(
      PublicKey(
        publicKey: rawPublicKey.decodeHex(),
        signatureAlgorithm: SignatureAlgorithm.ECDSA_P256
      ),
      hashAlgorithm: HashAlgorithm.SHA3_256,
      weight: weights[i],
    )
    i = i + 1
  }

  i = 0
  let signatureSet: [Crypto.KeyListSignature] = []
  for signature in signatures {
    signatureSet.append(
      Crypto.KeyListSignature(
        keyIndex: i,
        signature: signature.decodeHex()
      )
    )
    i = i + 1
  }

  return keyList.verify(
    signatureSet: signatureSet,
    signedData: message.decodeHex(),
    domainSeparationTag: "FLOW-V0.0-user"
  )
}