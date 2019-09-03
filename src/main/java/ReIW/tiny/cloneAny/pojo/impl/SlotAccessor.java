package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Map;

import ReIW.tiny.cloneAny.pojo.Accessor;

abstract class SlotAccessor implements Accessor {

	abstract SlotAccessor chown(String owner);

	abstract SlotAccessor rebind(Map<String, String> binds);

}
