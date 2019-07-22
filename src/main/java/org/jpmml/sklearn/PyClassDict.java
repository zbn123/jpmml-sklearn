/*
 * Copyright (c) 2017 Villu Ruusmann
 *
 * This file is part of JPMML-SkLearn
 *
 * JPMML-SkLearn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SkLearn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SkLearn.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.sklearn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import net.razorvine.pickle.objects.ClassDict;
import numpy.core.ScalarUtil;
import org.jpmml.converter.ValueUtil;

abstract
public class PyClassDict extends ClassDict {

	public PyClassDict(String module, String name){
		super(module, name);
	}

	public <E> E get(String name, Class<? extends E> clazz){
		Object value = get(name);

		if(value == null){

			if(!containsKey(name)){
				throw new IllegalArgumentException("Attribute \'" + ClassDictUtil.formatMember(this, name) + "\' not set");
			}

			throw new IllegalArgumentException("Attribute \'" + ClassDictUtil.formatMember(this, name) + "\' has a missing (None/null) value");
		}

		CastFunction<E> castFunction = new CastFunction<E>(clazz){

			@Override
			protected String formatMessage(Object object){
				return "Attribute \'" + ClassDictUtil.formatMember(PyClassDict.this, name) + "\' has an unsupported value (" + ClassDictUtil.formatClass(object) + ")";
			}
		};

		return castFunction.apply(value);
	}

	public <E> E getOptional(String name, Class<? extends E> clazz){
		Object value = get(name);

		if(value == null){
			return null;
		}

		return get(name, clazz);
	}

	public Boolean getBoolean(String name){
		return get(name, Boolean.class);
	}

	public Boolean getOptionalBoolean(String name, Boolean defaultValue){
		Boolean value = getOptional(name, Boolean.class);

		if(value == null){
			return defaultValue;
		}

		return value;
	}

	public Integer getInteger(String name){
		return ValueUtil.asInteger(getNumber(name));
	}

	public Number getNumber(String name){
		Object object = get(name, Object.class);

		object = ScalarUtil.decode(object);

		return (Number)object;
	}

	public Object getObject(String name){
		return get(name, Object.class);
	}

	public Object getOptionalObject(String name){
		return getOptional(name, Object.class);
	}

	public Object getScalar(String name){
		Object object = get(name);

		return ScalarUtil.decode(object);
	}

	public Object getOptionalScalar(String name){
		Object object = getOptional(name, Object.class);

		return ScalarUtil.decode(object);
	}

	public String getString(String name){
		return get(name, String.class);
	}

	public String getOptionalString(String name){
		return getOptional(name, String.class);
	}

	public Object[] getTuple(String name){
		return get(name, Object[].class);
	}

	public List<?> getArray(String name){
		Object object = get(name);

		if(object instanceof HasArray){
			HasArray hasArray = (HasArray)object;

			return hasArray.getArrayContent();
		} // End if

		if(object instanceof Number){
			return Collections.singletonList(object);
		}

		throw new IllegalArgumentException("The value of \'" + ClassDictUtil.formatMember(this, name) + "\' attribute (" + ClassDictUtil.formatClass(object) + ") is not a supported array type");
	}

	public List<Integer> getIntegerArray(String name){
		List<? extends Number> values = getArray(name, Number.class);

		return ValueUtil.asIntegers(values);
	}

	public <E> List<? extends E> getArray(String name, Class<? extends E> clazz){
		List<?> values = getArray(name);

		CastFunction<E> castFunction = new CastFunction<E>(clazz){

			@Override
			protected String formatMessage(Object object){
				return "Array attribute \'" + ClassDictUtil.formatMember(PyClassDict.this, name) + "\' contains an unsupported value (" + ClassDictUtil.formatClass(object) + ")";
			}
		};

		return Lists.transform(values, castFunction);
	}

	public int[] getArrayShape(String name){
		Object object = get(name);

		if(object instanceof HasArray){
			HasArray hasArray = (HasArray)object;

			return hasArray.getArrayShape();
		} // End if

		if(object instanceof Number){
			return new int[]{1};
		}

		throw new IllegalArgumentException("The value of \'" + ClassDictUtil.formatMember(this, name) + "\' attribute (" + ClassDictUtil.formatClass(object) +") is not a supported array type");
	}

	public int[] getArrayShape(String name, int length){
		int[] shape = getArrayShape(name);

		if(shape.length != length){
			throw new IllegalArgumentException("Expected " + length + "-dimensional array, got " + shape.length + "-dimensional (" + Arrays.toString(shape) + ") array");
		}

		return shape;
	}

	public List<?> getList(String name){
		return get(name, List.class);
	}

	public <E> List<E> getList(String name, Class<? extends E> clazz){
		List<?> values = getList(name);

		CastFunction<E> castFunction = new CastFunction<E>(clazz){

			@Override
			protected String formatMessage(Object object){
				return "List attribute \'" + ClassDictUtil.formatMember(PyClassDict.this, name) + "\' contains an unsupported value (" + ClassDictUtil.formatClass(object) + ")";
			}
		};

		return Lists.transform(values, castFunction);
	}

	public List<Object[]> getTupleList(String name){
		return getList(name, Object[].class);
	}

	public List<?> getListLike(String name){
		Object object = get(name);

		if(object instanceof HasArray){
			return getArray(name);
		} else

		{
			return getList(name);
		}
	}

	public <E> List<E> getListLike(String name, Class<? extends E> clazz){
		List<?> values = getListLike(name);

		CastFunction<E> castFunction = new CastFunction<E>(clazz){

			@Override
			protected String formatMessage(Object object){
				return "Array or list attribute \'" + ClassDictUtil.formatMember(PyClassDict.this, name) + "\' contains an unsupported value (" + ClassDictUtil.formatClass(object) + ")";
			}
		};

		return Lists.transform(values, castFunction);
	}
}
