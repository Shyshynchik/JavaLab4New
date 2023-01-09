import client.Cheque;
import client.Order;
import client.User;
import org.camunda.bpm.client.ExternalTaskClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Path("/user")
public class UserPaymentServlet {

    // http://localhost:8083/my-app/rest/user/order

    public Cheque generateCheque() {
        User user = new User();
        user.setNickName("Anime");
        user.setPassWord("Anime");
        Cheque cheque = new Cheque();
        cheque.setChequeCost(100);
        cheque.setChequePaid(true);
        Order order = new Order();
        order.setOrderUser(user);
        cheque.setOrder(order);
        return cheque;
    }

    @GET
    @Path("/order")
    @Produces({MediaType.APPLICATION_JSON})
    public Response returnPayment() throws IOException {

        Cheque cheque = generateCheque();

        String message;

        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest")
                .asyncResponseTimeout(10000) // long polling timeout
                .build();

        client.subscribe("UserAlertTopic")
                .lockDuration(1000) // the default lock duration is 20 seconds, but you can override this
                .handler((externalTask, externalTaskService) -> {

                    try {
                        Desktop.getDesktop().browse(new URI("https://docs.camunda.org/get-started/quick-start/complete"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Complete the task
                    externalTaskService.complete(externalTask);
                })
                .open();

        message = cheque.getOrder().getOrderUser().getNickName() + ", Your order has been successfully paid for! \n " +
                "The goods will be delivered to the pick-up point soon";
        return Response.ok(message).build();
    }
}
