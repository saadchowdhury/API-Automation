package util
import expression.CurlBaseVisitor
import expression.CurlLexer
import expression.CurlParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CodePointCharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import org.apache.commons.lang3.StringUtils
import model.ApiRequest

class CurlVisitor extends CurlBaseVisitor<ApiRequest> {
    static ApiRequest parseCurl(String curlCommand) {
        CodePointCharStream input = CharStreams.fromString(curlCommand)
        CurlLexer lexer = new CurlLexer(input)
        CommonTokenStream tokenStream = new CommonTokenStream(lexer)
        CurlParser parser = new CurlParser(tokenStream)
        ParseTree tree = parser.curl()
        CurlVisitor visitor = new CurlVisitor()
        visitor.visit(tree)
    }
    @Override
    ApiRequest visitCURLCOMMAND(CurlParser.CURLCOMMANDContext ctx) {
        String method = ctx.method.getText()
        String url = quoteRemover(ctx.URL().getText())
        Map headers = extractDataAsMap(ctx.HEADER())
        String data = ctx.DATA() == null ? null : quoteRemover(ctx.DATA().getText())
        ApiRequest apiRequest = ApiRequest.builder().method(method).url(url)
                .headerParams(headers).requestBody(data).build()
        apiRequest
    }

    private String quoteRemover(String value) {
        int startIndex = value.indexOf('\'')
        int endIndex = value.lastIndexOf('\'')
        value.substring(startIndex + 1, endIndex)
    }

    def extractDataAsMap(List<TerminalNode> nodes) {
        Map map = [:]

        String key, value
        nodes.each {
            header ->
                String h = quoteRemover(header.getText())
                if (h.trim().endsWith(";")) {
                    h = h.trim()
                    key = h.substring(0, h.size() - 1)
                    value = StringUtils.EMPTY
                } else {
                    (key, value) = h.split(":", 2)
                }
                map.put(key, value)
        }
        map
    }
}



