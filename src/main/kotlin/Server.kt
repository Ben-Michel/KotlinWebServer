import java.io.File
import java.io.OutputStream
import java.net.ServerSocket


class Server(port: Int) {
    private val server = ServerSocket(port)

    fun listen(){
        while(true){
            val app = server.accept()

            val inStream = app.getInputStream()
            val outStream = app.getOutputStream()

            var message =""
            var httpRequestLine =""
            var hasRequestLine = false


            while (true){
                val nextByte = inStream.read()
                message += nextByte.toChar()

                if(message.endsWith("\r\n") && !hasRequestLine){
                    hasRequestLine = true
                    httpRequestLine = message.dropLast(2)
                    message = ""
                }

                if(message.endsWith("\r\n\r\n"))
                    break
            }

            val httpRequestLineItems = httpRequestLine.split(" ")

            if ( httpRequestLineItems[0] == "GET") {
                val path = httpRequestLineItems[1]
                respond(path,outStream)
            }

            app.close()
        }
    }

    private fun respond(path: String, outStream: OutputStream){
        val folder = "./src/main/html"
        var target = ""
        if(path == "/"){
            target = "$folder/index.html"
        }else{
            target = "$folder$path"
        }
        if(fileExist(target)){
            val file = getFileContent(target)
            sendHttpResponse(code = "200 OK", outStream, file)
        } else {
            sendHttpResponse(code = "404 Not Found", outStream)
        }

    }

    private fun sendHttpResponse(code: String, outStream: OutputStream, file:String=""){
        outStream.write(("HTTP/1.1 $code\r\nContent-length:${file.toByteArray().size}\r\n\r\n" + file).toByteArray())
    }

    private fun getFileContent(path: String): String{
        return File(path).inputStream().readAllBytes().toString(Charsets.UTF_8)
    }
    private fun fileExist(path: String): Boolean{
        return File(path).exists()
    }

}