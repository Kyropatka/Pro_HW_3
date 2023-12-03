package academy.prog;

import jakarta.servlet.http.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/*
    POST(json) -> /add -> AddServlet -> MessageList
    GET -> /get?from=x -> GetListServlet <- MessageList
        <- json[...]
 */

public class AddServlet extends HttpServlet {

	private MessageList msgList = MessageList.getInstance();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		byte[] buf = requestBodyToArray(req); // json
        String bufStr = new String(buf, StandardCharsets.UTF_8);

		Message msg = Message.fromJSON(bufStr);
        if (msg != null) {
            System.out.println("Received message: " + msg.toString());

            if (msg.getText().startsWith("@")) {
                int endName = msg.getText().indexOf(' ');
                if (endName > 0) {
                    String recipient = msg.getText().substring(1, endName);
                    msg.setTo(recipient);
                    msg.setText(msg.getText().substring(endName + 1));

                    Message privateMsg = new Message(msg.getFrom(), msg.getTo(), msg.getText());
                    msgList.add(privateMsg);
                    return;
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
            msgList.add(msg);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
	}

	private byte[] requestBodyToArray(HttpServletRequest req) throws IOException {
        InputStream is = req.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[10240];
        int r;

        do {
            r = is.read(buf);
            if (r > 0) bos.write(buf, 0, r);
        } while (r != -1);

        return bos.toByteArray();
    }
}
