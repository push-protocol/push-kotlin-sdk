package push.kotlin.sdk
import org.json.JSONObject
import push.kotlin.sdk.channels.*
import kotlin.test.Test
import kotlin.test.assertEquals

class HelpersTest {
    @Test fun messageDecryptionTest() {
        val messageContent = "U2FsdGVkX18+UrnRfChnSk36MaqUQC7gD7r8aD2PKtI="
        val encryptedSecret = ENC_MESSAGE

        val decryptedMessage = Helpers.decryptMessage(encryptedSecret, messageContent, PGP_PK)
        assertEquals(decryptedMessage, "welcome to push")
    }

    @Test fun isEthValid() {
        val address1 = "0x355c8042605dEE474c1D5AF5705BC02Ae22351AB"
        val address2 = "0x355c804285dEE474a1D5AF57056BCdh6e22353AC"

        val expected1 = true
        val expected2 = false
        val real1 = Helpers.isValidAddress(address1)
        val real2 = Helpers.isValidAddress(address2)
        assertEquals(expected1, real1)
        assertEquals(expected2, real2)
    }

    @Test fun personal_signature () {
        val message = "This is good place to find a city"
        val sig = PublickKeyBuilder.sign(message)
        println("Sig was $sig")
    }

    @Test fun verifaction() {
        val random = "sometsfhing".toByteArray()
        val sign = PublickKeyBuilder.verifyData(random)
        println("signature was $sign")
    }

    @Test fun signerAbstract() {
        val privateKey = "c39d17b1575c8d5e6e615767e19dc285d1f803d21882fb0c60f7f5b7edb759b2"
        val message = "Create Push Profile \n252f10c83610ebca1a059c0bae8255eba2f95be4d1d7bcfa89d7248a82d9f111"
        val signer = PrivateKeySigner(privateKey)
        val signature = signer.getEip191Signature(message).getOrThrow()
        val address = signer.getAddress().getOrThrow()
        val expectedSignature = "0x9d71faa2582414160f3bc5b62bd8204b45e8ce60e42e034065366c27ed4f456d406ed157fec97db7cedb070f3488a0e78b6b59467063fab11aee6a2bf8233b131b"
        val expectedAddress = "0xd26a7bf7fa0f8f1f3f73b056c9a67565a6afe63c"
        assertEquals(expectedSignature, signature)
        assertEquals(expectedAddress, address)
        println(signature)
    }

    @Test fun testSignerCanDoEIP191SigV1() {
        val privateKey = "c39d17b1575c8d5e6e615767e19dc285d1f803d21882fb0c60f7f5b7edb759b2"
        val expected = "eip191:0x79725b6918f31cf01da680c8c11c8c6a208130c35459d64032444b7ba6b3b2cc447671d6c3be264fdfa08d5114cead9ca383f683809ec69f3c70c7101fc253221c"

        val signer = PrivateKeySigner(privateKey)
        val wallet = Wallet(signer)
        val signature = wallet.getEip191Signature("Enable Push Profile \n" +
                "c6f086dbc8295c8499873bf73e374f0bc230d567705c047938b3414163132280", "v1").getOrThrow()

        assertEquals(expected, signature)
    }

    @Test fun testSignerCanDoEIP191SigV2() {
        val privateKey = "c39d17b1575c8d5e6e615767e19dc285d1f803d21882fb0c60f7f5b7edb759b2"
        val expected = "eip191v2:0x79725b6918f31cf01da680c8c11c8c6a208130c35459d64032444b7ba6b3b2cc447671d6c3be264fdfa08d5114cead9ca383f683809ec69f3c70c7101fc253221c"

        val signer = PrivateKeySigner(privateKey)
        val wallet = Wallet(signer)
        val signature = wallet.getEip191Signature("Enable Push Profile \n" +
                "c6f086dbc8295c8499873bf73e374f0bc230d567705c047938b3414163132280", "v2").getOrThrow()

        assertEquals(expected, signature)
    }

    @Test
    fun decryptPgpKeyTest() {
        val privateKey = "c39d17b1575c8d5e6e615767e19dc285d1f803d21882fb0c60f7f5b7edb759b2"
        val signer = PrivateKeySigner(privateKey)

        val encryptedPrivateKey = "{\"ciphertext\":\"7cd4b88bd8a1e332dc83f1e94c56de3ec0e1f7f98a223c09fef77afa26c98bac966e755d450faae763c896b0c2326d5fead9b7bbc56cc232a4f1ca15b460109db8c44c0007471a6cda0a2d44d6ebcb6dcc42c242a7557a97993a9275cb40f90600ef5c62fd50c6557f88241da02a03527a38c8e555d0695afd509059fec596ddd3b285d65cb22595a4fb4209e09fe5d3c48211dfc27c7048916e2bffd4e0aa44b98f835c5d0fb63cb5e5d1b7845db83db7ccf0a9095c7e149f5d5f37c419d73f893fe6f1511681ed1679c7c25991ae7acd8b5d6718ab0d45385f60130bb8ccd4d0a36e6fc69f71c92fcc7e63e08e650bfdda767b2acb749497c3b968cb47649f488c25c5a9bd0ed15e0701bb1dd9a9d6a52f8084fd7897efe159408bf4ee85c62e33e89718591c8e85485db0c62d33aa5e7703bac95ca0bb194f558d53b6af2b966735305850156876ddbad3ff55888e93c2d0c826b89c7c76ea8f23cb39566d57b4eeaf207a267bfb46cc0fa957ba5c7d1b315a4534404ae163dbf7fa252ae7d4cbfd4341a17486f7d2633728a095e84aad2e7eac898435b5045c353cf7128ec94451fbc7aca1f3b402efcac680ac90872c984f7943ba22fe28aea590e8e18ee020ca30cfbb6fba4fadbdf178fbc51d3a6f33d3182eb1c84901cf6f1ad0c4ef497c7d6503002bfe43fe412fe711c3ad803c4a5eb800b5f32d5142a2d99062dcc81b85efd70ab7de64fed00f83c180c5a41288c3ed242155a00dd09fe4efb84e9c277b2207f9a755700d9326c9c13edc5a8990206e0964e3d722f4f50fbf5a742b30399efbf0f67fc73ca9fd8e609516b332b42b02f9ec9e25e2daf1824037c169a12eae20b9eaa342c2f4ffff0110df666a44cdabac74dd3f575934f3c070d2fa795738dd9a258471a62f563d78e9f590a8ba0352b9757c20046332bfc2e68403dc5cf678f58614ee976258471c3f26c6de3762e2a23d143cbf3f1ac5f060bd38461358d8bcb73f2f5a31316bd094a6384d2ae9c4efe9e0e1c79f0b13cfc03b59e1abf791ede2197ef1bcd7f9c9d9ea3b9d37fce6497c69a8ab4c4e8c62d981ab6d22ed3d0abd1985974a5eb39f45d53de8660019dd9c99796cc8f6161e527dc70fb2ef39beea311070e077b0f42e4633cfc6a71cd1a63b52b8fbe7439ded3a179793d23009ca28994f0c968c2deb536b39c948014112387eb2a458ebc587225126e0bff51fa76f3d4d6ec5d1074f1b3c3b9a5808e48ac2efe1994f0b358c56b7bfa86f3d862eb7bac17f66dcbd58e9593c5e12bca72ad64ce988eccefc20734b4a6fe774369388dfc39f3169abbc7f81914a8887c04cfb3800f1daa4ea919a679a86df88a6b11c5dd4d110e33a6a0a7191201f698dac1ff6800b9268d49d3327d1a5ad3224cb140e365a5c760b30c089da64efef3dbf99e4fad08a8f39b8f7e6cf2fc5fe3686a80d8164be8e67c91e338bc133d8d773e2b826a72f9b6717b7989cf000141d2e465bf9e184909de92d1137e6b4c25bab0010c175e65eae0d4028366b1fb894d6bd9960a65fcaadbcea1a0615778d43958392a74cc91237ef58235ed0211c790309e1dc4d1d09ec884f8caebd9f3e7a2eb1344179bf28787f347a0ef7401941e72daf6a486ccf0d0d8cec8e35cc124d2c94a8f180867eae3e11bdae87548c2a412aca3791fcfe0d442338bdb75267e54e762c30ee544a01e51c8da8ec565b142a1b2674fd4abe8d73cc73e82f820530cd01a10074ce34f6d461e110997eabcbc556d98bb99725a2874bd8acb27ef84b75d7d5dfa84771057872c63c8312ffc5b2ea6059086a90f4327e9989721db3064962919e9c6e3bb45248f662bd9066c1a0d2f165fe93fb1306f1bda16db2fc285c200382d7c0ef31cda3179eedbd47343660e639fd98a11b081fcd1b604b5e161817e144790658826b7d7436895e08e8e75fc8e0d7281edfb2adee849900cb132e14a02ae075ded3ed4e9874900e72b8d7497e5fbdca43e09f5bdef2f51ac19f10d1d9dd99062a325692888330abb385a0a40b29cf4a8e0db32a62e039c1a4f67efc82c30c36150560bf6e2dbbd8249f4cb9c1be5bd5c6bb0d048112a258d4320a025842bff51a54194ee168c483dbb7f4a38e297dce593d84f602ec0a51820e7770edcf8b9f3612a8760ac9bcbcb73581205ff124c4bae709694c705452cf9fb755a7944d2549f4498f676bbbf587e20e98dab8a702391863bc8051854e22b0680ec2e88273ae0fd7d09c4163b75c438f96f4a6f6399507f4f31dc13e68814d9858b11ced36759b26d4845384250632bcab6639e00ae59a026ae6db8df9bd5c128b8f53b1bfef5a6b5065580394b2229439fb4e25c732ec8a2dd4e9dd4c687eadd7b527ac6c8dd29ee870b0d8360ef60b20bf9a894358691ba3cdc2dcb4c19fbad43caad438a90c353b007ea16d216007f0e7b2ca5aee59ac359b1ab586d3cdd31c54ae6e34064aaaec5f67959f3a248f9ccca41dd9c157f19a0a001676a26a204461eecd7e4d77e00042ce20e735fea7364683ae07330c475dc7ec82c7b306b7281af0e9b9e6a16e32c4693d1b35c81a397801624a7a4223a39905f8c4da098598f2fc880c6ea82a74f4c2230e59463f7690a4a70193d6b219587d752291107d125a50ba24ff58b5894c29843dea786ee3ee0a6b0b2fdd2a081148c2157b1477120d3a1e4a41e51083ee75e8f7cb6e901db814cc7549ca22587472e384499e6df775416687e6d838a85f85c177cf8bde16fe6d7e221bf76c68e88ab85cc3f27332c24ec4978defd0438ba32ed4bba272b6344357005d3c3aa909e11e49b311453ca6664beee11d7c3abdac7183036eb142630ebb7c17879e05d9386179ee1d8e3f0e42d59735a87d6a58c405270a96c1e8159166c02502cf5b982d59521f99c9a6450f53ead70e06aca66de4aa4d01d6925b0600e453317c3336dbaa2d40e2bcb965c0e94f8a572b09d2c279681c56d2e3c901a5ebca6e6c46797b6ce46cb107188338f627bab3a07cb1c69e975a7585b6b11dbf47b299d1064d1c5f1529e81d90382825b58bd73f37c873320bb06c62e0ec7643de697ffcda138cc54b4aa9a707d9030acd833fcf022c5891a36d2723175b3ddf6365e1fa2c5f676681e81434a23812fe9f60988cb645f54b671abca9088afba10e9fc3c09b1e4ce872c204465a758632e74e4f36791aafa89bafd1e4352bccde36925560b0954811ec0d7d817489d7d7ccf0590553bbc341134b619b9967abb989e523bbcf6a24fd0fc67cb4ee0dcf8845fe5ccf9290ccb79d10a0d9fabc8fdb06b424c3e249df325cdf7a59e4d9cccc64eca7c2cbe72ca2d8644e2883fd1968b7b62eaf8c8b547de6e354f233c8e679e1836fd9e9834951f72d2c6731460ea31331efff8120f3918139afd29851346893ccbc967f16e89f4a61058220c910678d328fdfadfb4fe00b835c35f7f284ef47ae47fdc8e409596d8afc0777a257156cfa4388fae8e9775779de98df01394fff4b86b4d353648ecce41f3ea35ed578a14077a06b8ec48de8d9fa8c499acc426e79be34d71dc31ff66da81ce18a1c67300822d163bb50594a4b681f801d4a1c4f4f4e21330bfcf4930ac7839eb470cd95abea362908ebc6a2604423df1c3fa97320335bee7b3bb0e0c1dc5ed77e800cb20978f22f5ce545527c4069b93b140f6d938dc12e4ab70f77f2621d5c5c8ba4afb5c12b58d4fd0b5c3d3257488fe31cc922270989934116e36048253cd71d26d04d449759c5fcdfaff8c16b37914578b783279fe32d52bd398398a6b2f7d5f827d839e812353fab5267e43f997aca3194edcb7b7a137ab70bb1f3f26be775a6adbc05d4d49b740527c448957c0c5aa32b997c3b0cc8b307a7144ecbfa3c266382a457b3e933c2afa9119c6f2fae1aec63f275a0220e8c0616dd02a2c4a28197c1f84d5a061bf4908623ec81e43aa5b7e9ea4ac0bc44ab775f1789eac73111b4e0c7d82fa7c8b498aab747febd073ed2cdf988bafc60d0f3620f5ec940b51c57762a711b5e39a248fffe1f942b08fc73dbc421d49f4a8ea993068c823ff05e098d1cdad95db181037ace6b2e0abdc850f2119b7fa0c01fefd01d7dac77c3498b9211ded286b85330fa1c617f48d8afa3e8f162e8e86f79e215f2eed135d10b53894f422e934715978c99f2ebf2e408c65da2206c1f7754f4df21ca0c407ff61396643b7d221ae2738d4e81caf5fc22ceefc1848d6c042c3be1455b03450c2f2cd2b7c49ebd133a70dfe3995e1900814865215f291f5eb62c4f2c9c3630b71d0fd4da4e93480e2bb9cbed7c5cdadea0d14cd5b231918303ddd929eea9a936b11c701dd4a5628647857421e4594d2ce3f2a13e1059a404443653c6ca471cb4c45d8fb1199e29b833815ca270eb5a8d0c909f9fda750a5ba8049dbabffcb3ba58de204eccffae691b1a450572adaa14d32d3a1b07a6659dc91b92deca0a51993665fbdcbfedf10d191a384cafd074a12b615a2f97c5179358986db5dfcb5436e8ed8a3ec7f096c3f979dcd694630e00e2428d70e50fa5c52c23f31781bb290e2298f10d335b16a5743ae68c1ffd78ee495d6b51768d0c18dc9e58d6c488430e4953739e7229c1eea6d7531745d5f2e8e8ae10d8665574606f45cf62a0e050ff73603083cc96c0472450996dbe43500d2c0d2caf11b94f32b38279e37b24a13ed7f2759f9b895b28e1079aa8391efb84ee4c34f639fc25056099f4b6f94db35b80ba91369264ed2012be487117f439b8e100036a7a8ab2bc0b14b9531561e81b1cba3b6a773bed9ff10ba51168f9ee7af563a237201803bf8341aa44dec93d3a7483d5ee581533ab4f264b9ba11e565ed6bb2b5daa9e10bf2ee29e04f2498c0a23501407a6e884badab7c479c31c385d4ca4bfb46daba447af8a551d1fa003\",\"version\":\"eip191-aes256-gcm-hkdf-sha256\",\"salt\":\"8df3cc0bbc7434b2ed7ca674b13d6c1180a5300fec3f09ad6ad9f8c8cc2a26ba\",\"nonce\":\"dfc04cc8782887903338f7e3\",\"preKey\":\"c6f086dbc8295c8499873bf73e374f0bc230d567705c047938b3414163132280\"}\n"
        val expectedPrefix = "-----BEGIN PGP PRIVATE KEY BLOCK-----"
        val expectedSuffix = "-----END PGP PRIVATE KEY BLOCK-----"

        val decryptedPgpKey = DecryptPgp.decryptPgpKey(encryptedPrivateKey,signer).getOrThrow()
        val parts = decryptedPgpKey.split("\n")
        val actualPrefix = parts[0]
        val actualSuffix = parts[parts.size - 1]
        assertEquals(expectedPrefix, actualPrefix)
        assertEquals(expectedSuffix, actualSuffix)
    }

    @Test
    fun GettingChannel(){
        val expectedChannel = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78"
        val options = ChannelOptions("0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", ENV.staging, 1, 1)
        val res =  Channel.getChannel(options)
//        println(actualChannel)
        val jsonObject = JSONObject(res)
        val actualChannel = jsonObject.getString("channel")
        println(actualChannel)
        assertEquals(expectedChannel, actualChannel)
    }

    @Test
    fun GettingAllChannels () {
//        val res = Channel.getAllChannels(ENV.staging, 1, 10)
        val options = ChannelOptions("",ENV.staging, 1, 10)
        val res = Channel.getAllChannels(options)
        val jsonObject = JSONObject(res)
        val expectedCount = 10
        val actualChannels = jsonObject.getJSONArray("channels")
        println(actualChannels.length())
        assertEquals(expectedCount, actualChannels.length())
    }

    @Test
    fun NonExistingChannels() {
        val userAddress = "0xcD23560D4F9F816Ffb3D790e5ac3f6A316c559Ea"
        val options = ChannelOptions(userAddress, ENV.staging, 1, 1)
        val res =  Channel.getChannel(options)
        println(res)
//        assertEquals(expectedChannel, actualChannel)
    }

    @Test
    fun SearchChannelByName() {
        val res = Search.searchChannels(ENV.staging, 1, 10, "desc", "rayan")
        println(res)
        val actual = JSONObject(res)
        val actualCount = actual.getInt("itemcount")
        val asExpected = actualCount > 0
        assertEquals(true, asExpected)
    }

    @Test
    fun getSubscriberTest() {
        val res = Subscribe.getSubscribers("0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", ENV.staging,5, 1)
        val Data = JSONObject(res)
        val actual = Data.getJSONArray("subscribers")[0]
        val expected = "0x02b24ac2239b344fbc4577801f7000901e7a3944"
        assertEquals(expected, actual)
    }

    @Test
    fun isSubscribed() {
        val res1 = Subscribe.IsSubscribed("0x5d73D70EB962083eDED53F03e2D4fA7d7773c4CE", "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", ENV.staging)
        val res2 = Subscribe.IsSubscribed("0x361158064636d05198b23389c75ee32fa10b26bd", "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", ENV.staging)
        println("resss $res1")
        assertEquals(false, res1)
        assertEquals(true, res2)
    }

    @Test
    fun SearchChannelByAddress() {
        val res = Search.searchChannels(ENV.staging, 1, 10, "desc", "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78")
        val jsonObject = JSONObject(res)
        val actual = jsonObject.getJSONArray("channels")

        if (actual.length() > 0) {
            val firstChannel = actual.getJSONObject(0)
            val channelAddress = firstChannel.getString("channel")
            println(channelAddress)
            assertEquals("0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", channelAddress)
        }
    }

    @Test
    fun testNoexistingChannels() {

    }

    @Test
    fun OptingINchannels() {
        val options = Opt.OptInChannel(ENV.staging, "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", "0xEDF59F183584107B20e2c95189A4423224bba8F2", "c39d17b1575c8d5e6e615767e19dc285d1f803d21882fb0c60f7f5b7edb759b2")
        println(options)
    }
}