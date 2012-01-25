package ch.bind.philib.net;

public interface ConsumerFactory {

    Consumer acceptConnection(Connection connection);

}
