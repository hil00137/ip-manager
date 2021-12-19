import java.io.File
import java.util.regex.Pattern

class IpManager(private val filePath: String) {
    private val p = Pattern.compile("deny ([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})/([0-9]{1,2});")
    private var addIp = false
    private fun readFile() {
        File(filePath).forEachLine {
            val matcher = p.matcher(it);
            if (matcher.matches()) {
                val ip1 = matcher.group(1)
                val ip2 = matcher.group(2)
                val ip3 = matcher.group(3)
                val ip4 = matcher.group(4)
                val masking = matcher.group(5)
                list.add(IpNode(ip1, ip2, ip3, ip4, masking));
            }
        }
        list.sortBy { it.start }
    }

    fun addIp(ip: String) {
        this.readFile()
        this.checkIp(ip)
        this.merge()
        this.writeFile()
    }

    private fun checkIp(ip: String) {
        val ipNode = IpNode(ip)
        val currentIp = ipNode.start
        var status = Action.ADD
        var prevNode: IpNode? = null
        for (index in 0 until list.size) {
            val currentNode = list[index]
            if ((prevNode == null || prevNode.end < currentIp) && (currentIp < currentNode.start)) {
                status = Action.ADD
                break
            } else if (currentNode.start <= currentIp && currentIp <= currentNode.end) {
                println("skip!")
                status = Action.SKIP
                break
            }
            prevNode = currentNode
        }
        if (status == Action.SKIP) {
            return
        } else if (status == Action.ADD) {
            println("add $ip")
            addIp = true
            list.add(ipNode)
            list.sortBy { it.start }
        }
    }

    private fun merge() {
        var repeat = true
        while (repeat) {
            repeat = false
            for (index in 0 until list.size) {
                val currentNode = list[index]
                val nextNode = if (index != list.size - 1) {
                    list[index + 1]
                } else break
                if (currentNode.masking == nextNode.masking && currentNode.end + 1 == nextNode.start) {
                    val merge = currentNode.merge()
                    if (merge != null) {
                        list.remove(currentNode)
                        list.remove(nextNode)
                        list.add(merge)
                        repeat = true
                        println("merge")
                        list.sortBy { it.start }
                        break
                    }
                }
            }
        }
    }

    private fun writeFile() {
        val file = File(filePath)
        file.writeText("")
        list.forEach {
            file.appendText("deny ${it.getIp()};\n")
        }
    }

    var list = ArrayList<IpNode>()
    enum class Action {
        ADD, SKIP
    }
}

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("you need ips.conf, ip")
        return
    }
    val fileName = args[0]
    val ip = args[1]
    val ipManager = IpManager(fileName)
    ipManager.addIp(ip)
}