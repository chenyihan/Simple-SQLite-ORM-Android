package org.cyy.fw.android.dborm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectUtil {
	public static String[] toStringArray(Object[] values) {
		if (values == null) {
			return null;
		}
		String[] strArr = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null) {
				strArr[i] = null;
			} else
				strArr[i] = values[i].toString();
		}
		return strArr;
	}

	public static boolean isArrayEmpty(Object[] array) {
		return (array == null) || (array.length == 0);
	}

	public static boolean isArrayEmpty(long[] array) {
		return (array == null) || (array.length == 0);
	}

	public static boolean isBoolean(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("invalid parameter.");
		}
		Class<?> clazz = object.getClass();
		return ReflectUtil.isBooleanType(clazz);
	}

	public static boolean isByteType(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("invalid parameter.");
		}
		Class<?> clazz = object.getClass();
		return ReflectUtil.isByteType(clazz);
	}

	public static boolean isChar(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("invalid parameter.");
		}
		Class<?> clazz = object.getClass();
		return ReflectUtil.isCharType(clazz);
	}

	public static boolean isDouble(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("invalid parameter.");
		}
		Class<?> clazz = object.getClass();
		return ReflectUtil.isDoubleType(clazz);
	}

	public static boolean isEqual(Object obj1, Object obj2) {
		if (obj1 == null) {
			return obj2 == null;
		}
		return obj1.equals(obj2);
	}

	public static boolean isFloat(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("invalid parameter.");
		}
		Class<?> clazz = object.getClass();
		return ReflectUtil.isFloatType(clazz);
	}

	public static boolean isInteger(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("invalid parameter.");
		}
		Class<?> clazz = object.getClass();
		return ReflectUtil.isIntegerType(clazz);
	}

	public static boolean isLong(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("invalid parameter.");
		}
		Class<?> clazz = object.getClass();
		return ReflectUtil.isLongType(clazz);
	}

	public static boolean isPrimitiveType(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("invalid parameter.");
		}
		Class<?> clazz = object.getClass();
		return ReflectUtil.isPrimitiveType(clazz);
	}

	public static boolean isShort(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("invalid parameter.");
		}
		Class<?> clazz = object.getClass();
		return ReflectUtil.isShortType(clazz);
	}

	public static <T extends Serializable> int sizeOf(T obj) {
		try {
			ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
			ObjectOutputStream objOutputStream = new ObjectOutputStream(byteOs);
			objOutputStream.writeObject(obj);
			int size = byteOs.size();
			return size;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
