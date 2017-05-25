/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.fr.bi.stable.structure.array;

/** A resizable, ordered or unordered int array. Avoids the boxing that occurs with ArrayList<Integer>. If unordered, this class
 * avoids a memory copy when removing elements (the last element is moved to the removed element's position).
 * @author Nathan Sweet */
public class DirectIntList implements IntList{
	public DirectIntArray items;
	public int size;
	protected DirectIntList() {
		this(16);
	}

	protected DirectIntList(int capacity) {
		items = new DirectIntArray(capacity);
	}

	protected DirectIntList(int capacity, int defaultValue) {
		items = new DirectIntArray(capacity, defaultValue);
	}


	public void add (int value) {
		DirectIntArray items = this.items;
		if (size == items.size()){
			items = resize(Math.max(8, (int)(size * 1.75f)));
		}
		items.put(size++,value);
	}


	public int get (int index) {
		if (index >= size) {
			throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		}
		return items.get(index);
	}

	public void set (int index, int value) {
		if (index >= size) {
			throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		}
		items.put(index, value);
	}

	@Override
	public int size() {
		return size;
	}

	public void clear () {
		size = 0;
		items.release();
	}

	protected DirectIntArray resize (int newSize) {
		DirectIntArray newItems = new DirectIntArray(items, newSize);
		this.items = newItems;
		return newItems;
	}

	public String toString () {
		if (size == 0) return "[]";
		DirectIntArray items = this.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		buffer.append(items.get(0));
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(items.get(i));
		}
		buffer.append(']');
		return buffer.toString();
	}

}
