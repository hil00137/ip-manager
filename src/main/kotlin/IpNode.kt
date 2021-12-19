class IpNode {
    private var ip1: String = ""
    private var ip2: String = ""
    private var ip3: String = ""
    private var ip4: String = ""
    var masking: String = "32"
    var start: Long = 0L
    var end: Long = 0L

    constructor(ip1: String, ip2: String, ip3: String, ip4: String, masking: String = "32") {
        this.ip1 = ip1
        this.ip2 = ip2
        this.ip3 = ip3
        this.ip4 = ip4
        this.masking = masking
        parse()
    }
    constructor(ip: String) {
        val split = ip.split(".")
        ip1 = split[0]
        ip2 = split[1]
        ip3 = split[2]
        ip4 = split[3]
        parse()
    }

    private fun parse() {
        val ip1S = ip1.toLong().toString(radix = 2).padStart(8, '0')
        val ip2S = ip2.toLong().toString(radix = 2).padStart(8, '0')
        val ip3S = ip3.toLong().toString(radix = 2).padStart(8, '0')
        val ip4S = ip4.toLong().toString(radix = 2).padStart(8, '0')
        val mask = masking.toInt()
        val gap = if (mask == 32) {
            0
        } else {
            invertMask(32 - mask).toLong(radix = 2)
        }
        val maskingI = mask(mask)
        val ip = ip1S + ip2S + ip3S + ip4S
        start = ip.toLong(radix = 2) and maskingI.toLong(radix = 2)
        end = start + gap
    }

    private fun mask(maskingI: Int): String {
        var s = ""
        for (i in 0 until maskingI) {
            s += "1"
        }
        return s.padEnd(32, '0');
    }

    private fun invertMask(maskingI: Int): String {
        var s = ""
        for (i in 0 until maskingI) {
            s += "1"
        }
        return s
    }

    fun getIp(): String {
        return listOf(ip1, ip2, ip3, ip4).joinToString(".") + "/" + masking
    }

    fun merge(): IpNode? {
        val tempNode = IpNode(this.ip1, this.ip2, this.ip3, this.ip4, (masking.toInt() - 1).toString())
        if (tempNode.start == start) {
            return tempNode
        }
        return null
    }
}