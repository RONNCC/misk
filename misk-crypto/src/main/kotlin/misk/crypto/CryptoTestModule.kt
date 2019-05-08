package misk.crypto

import com.google.crypto.tink.Aead
import com.google.crypto.tink.Mac
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.mac.MacConfig
import com.google.crypto.tink.signature.SignatureConfig
import com.google.inject.name.Names
import misk.inject.KAbstractModule
import misk.resources.ResourceLoaderModule

/**
 * This module should be used for testing purposes only.
 * It generates random keys for each key name specified in the configuration
 * and uses [FakeKmsClient] instead of a real KMS service.
 *
 * This module **will** read the exact same configuration files as the real application,
 * but **will not** use the key material specified in the configuration.
 * Instead, it'll generate a random keyset handle for each named key.
 */
class CryptoTestModule(
  private val keyNames: List<Key>
) : KAbstractModule() {

  override fun configure() {

    AeadConfig.register()
    MacConfig.register()
    SignatureConfig.register()

    keyNames.forEach { key ->
      when (key.key_type) {
        KeyType.AEAD -> {
          bind<Aead>()
              .annotatedWith(Names.named(key.key_name))
              .toProvider(AeadEnvelopeProvider(key,null))
              .asEagerSingleton()
        }
        KeyType.MAC -> {
          bind<Mac>()
              .annotatedWith(Names.named(key.key_name))
              .toProvider(MacProvider(key, null))
              .asEagerSingleton()
        }
        KeyType.DIGITAL_SIGNATURE -> {
          // val keysetHandle = KeysetHandle.generateNew(SignatureKeyTemplates.ED25519)
          // val signer = PublicKeySignFactory.getPrimitive(keysetHandle)
          // val verifier = PublicKeyVerifyFactory.getPrimitive(keysetHandle.publicKeysetHandle)
          bind<PublicKeySign>()
              .annotatedWith(Names.named(key.key_name))
              .toProvider(DigitalSignatureSignerProvider(key, null))
              .asEagerSingleton()
          bind<PublicKeyVerify>()
              .annotatedWith(Names.named(key.key_name))
              .toProvider(DigitalSignatureVerifierProvider(key, null))
              .asEagerSingleton()
          }
        }
      }
    }
}

