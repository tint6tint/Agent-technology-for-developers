package fi.jyu.ties454.yajiliu.assignment1.task5;

public class Message {
	private String topic;
	private int content;
	
	public Message(String topic, int content) {
		this.topic = topic;
		this.content = content;
	}

	public Message() {
		
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getContent() {
		return content;
	}

	public void setContent(int content) {
		this.content = content;
	}	
}
