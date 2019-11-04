package ReIW.tiny.cloneAny.pojo;

import java.util.List;

// TODO typeParam が + とか - の場合は実行時型をつかって Ditto つくる必要あるかもしれんし Object のときとかもそうかも
// isSuper とか isExtends とかあったらいけそう？

public interface Slot {

	String getDescriptor();
	
	String getSignature();

	boolean isArray();

	List<Slot> elementSlot();

}
