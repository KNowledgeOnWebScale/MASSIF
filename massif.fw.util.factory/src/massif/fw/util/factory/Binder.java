package massif.fw.util.factory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import massif.journal.api.JournalService;
@Component
public class Binder {
	
	private JournalService jsservice;
	private MassifFactory mFactory;
	
	public Binder(){
		mFactory = MassifFactory.getInstance();
	}
	@Reference(unbind="unbindJournalService")
	public void bindJournalService(JournalService js) {
		this.jsservice = js;
		mFactory.setJournalService(js);
	}
	public void unbindJournalService(JournalService js) {
		this.jsservice = js;
	}

}
