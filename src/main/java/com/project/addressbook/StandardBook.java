package com.project.addressbook;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class StandardBook implements BookExt {
	protected final Map<Name, Set<Target>> addresses;

	public StandardBook() {
		super();
		addresses = new HashMap<Name, Set<Target>>();
	}

	@Override
	public boolean add(Name name, Target target) {
		Set<Target> targets = addresses.get(name);
		if ((target instanceof Alias) && addresses.get(target) == null) {
			throw new IllegalArgumentException();
		}
		if (targets == null) {
			targets = new HashSet<Target>();
			addresses.put(name, targets);
		} else if (name instanceof Alias) {
			if (targets.contains(target)) {
				return false;
			}
			throw new IllegalArgumentException();
		}
		return targets.add(target);
	}

	@Override
	public boolean delete(Name name, Target target) {
		Set<Target> targets = addresses.get(name);
		if (targets != null && targets.contains(target)) {
			if (targets.size() > 1) {
				return targets.remove(target);
			}
			if (name instanceof Group && targets.size() == 1) {
				throw new IllegalArgumentException();
			}
			return addresses.remove(name) != null;
		}
		return false;
	}

	@Override
	public Set<Address> lookup(Name name) {
		Preconditions.checkNotNull(name, "name");
		Set<Address> results = new HashSet<Address>();
		Set<Target> targets = addresses.get(name);
		Stack<Target> stack = new Stack<Target>();
		Stack<Target> looked = new Stack<Target>();
		if (targets != null) {
			stack.addAll(targets);
			while (!stack.empty()) {
				Target target = stack.pop();
				if (target instanceof Address) {
					results.add((Address) target);
				} else if(!looked.contains(target)) {
					// resolve alias or group
					stack.addAll(addresses.get(target));
					looked.add(target);
				}
			}
		}
		return results;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String NEW_INDENT = " ";
		String NEW_LINE = System.getProperty("line.separator");
		sb.append("{Book ");
		for (Map.Entry<Name, Set<Target>> addr : addresses.entrySet()) {
			sb.append(NEW_LINE).append(NEW_INDENT).append(addr.getKey()).append(" => [");
			boolean first = true;
			for (Target t : addr.getValue()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(t);
			}
			sb.append("]");
		}
		sb.append(NEW_LINE).append("}EndBook");
		return sb.toString();
	}

	@Override
	public Iterator<Name> getNames(boolean sortAsc) {
		Comparator<Name> accComp = (Name name1, Name name2) -> name1.getValue().compareTo(name2.getValue());
		if (sortAsc) {
			return addresses.keySet().stream().sorted(accComp).collect(Collectors.toList()).iterator();
		} else {
			return addresses.keySet().stream().sorted(accComp.reversed()).collect(Collectors.toList()).iterator();
		}
	}

	@Override
	public Set<Name> lookup(Address address) {
		Preconditions.checkNotNull(address, "name");
		Set<Name> results = new HashSet<Name>();
		Stack<Target> stack = new Stack<Target>();
		stack.push(address);
		while (!stack.empty()) {
			Target add = stack.pop();
			Set<Name> names = addresses.entrySet().stream().filter(entry -> {
				return entry.getValue().contains(add);
			}).map(Map.Entry::getKey).collect(Collectors.toSet());
			
			results.addAll(names);
			names.removeAll(stack);
			stack.addAll(names);
		}
		return results;
	}

}
