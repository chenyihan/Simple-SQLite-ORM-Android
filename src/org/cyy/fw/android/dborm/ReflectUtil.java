package org.cyy.fw.android.dborm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import android.text.TextUtils;
import android.util.Log;

public class ReflectUtil {

	public static final String TAG = "ReflectUtil";
	private static final WeakHashMap<Field, Method> setterCache = new WeakHashMap<Field, Method>();

	private static final WeakHashMap<Field, Method> getterCache = new WeakHashMap<Field, Method>();

	public static String generateGetterName(String fieldName) {
		return generateGetterName(fieldName, false);
	}

	public static String generateGetterName(String fieldName,
			boolean isPrimitiveBoolean) {
		String firstCH = "" + fieldName.charAt(0);
		Character secondCH = null;
		if (fieldName.length() > 1) {
			secondCH = Character.valueOf(fieldName.charAt(1));
		}
		String getterName = null;
		String replaceStr = null;
		if ((secondCH == null)
				|| (!Character.isUpperCase(secondCH.charValue()))) {
			replaceStr = fieldName.replaceFirst(firstCH, firstCH.toUpperCase());
		} else {
			replaceStr = fieldName;
		}
		if (isPrimitiveBoolean) {
			getterName = "is" + replaceStr;
		} else {
			getterName = "get" + replaceStr;
		}
		return getterName;
	}

	public static String generateSetterName(String fieldName) {
		if (TextUtils.isEmpty(fieldName)) {
			throw new IllegalArgumentException("invalid field.");
		}
		String firstCH = fieldName.substring(0);
		Character secondCH = null;
		if (fieldName.length() > 1) {
			secondCH = Character.valueOf(fieldName.charAt(1));
		}
		String setterName = null;
		if ((secondCH == null)
				|| (!Character.isUpperCase(secondCH.charValue()))) {
			setterName = "set"
					+ fieldName.replaceFirst(firstCH, firstCH.toUpperCase());
		} else {
			setterName = "set" + fieldName;
		}
		return setterName;
	}

	public static Field getDeclaredField(Object object, String fieldName) {
		Field field = null;

		Class<?> clazz = object.getClass();

		while (clazz != Object.class) {
			try {
				field = clazz.getDeclaredField(fieldName);
				return field;
			} catch (Exception e) {
				Log.i("ReflectUtil File", "");

				clazz = clazz.getSuperclass();
			}

		}

		return null;
	}

	public static List<Field[]> getDeclaredFieldsIncludeInherit(Class<?> clazz) {
		List<Field[]> list = new ArrayList<Field[]>();
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			Field[] fields = c.getDeclaredFields();
			list.add(fields);
		}
		return list;
	}

	public static Method getDeclaredMethod(Object object, String methodName,
			Class<?>[] parameterTypes) {
		Method method = null;

		for (Class<?> clazz = object.getClass(); clazz != Object.class;) {
			try {
				method = clazz.getDeclaredMethod(methodName, parameterTypes);
				return method;
			} catch (Exception e) {
				Log.i("ReflectUtil File", "");

				clazz = clazz.getSuperclass();
			}

		}

		return null;
	}

	public static Method getGetter(Field field) {
		String fieldName = field.getName();
		Method getter = (Method) getterCache.get(field);
		if (getter != null) {
			return getter;
		}
		String methodName = generateGetterName(fieldName,
				field.getType() == Boolean.TYPE);
		Class<?> clazz = field.getDeclaringClass();
		try {
			getter = clazz.getMethod(methodName, (Class<?>[]) null);
			getterCache.put(field, getter);
			return getter;
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchMethodException e) {
		}
		return null;
	}

	public static Method getSetter(Field field) {
		String fieldName = field.getName();
		Method setter = (Method) setterCache.get(field);
		if (setter != null) {
			return setter;
		}
		String methodName = generateSetterName(fieldName);
		Class<?> clazz = field.getDeclaringClass();
		try {
			setter = clazz.getMethod(methodName,
					new Class[] { field.getType() });
			setterCache.put(field, setter);
			return setter;
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchMethodException e) {
		}
		return null;
	}

	public static Object getValue(Object object, String fieldName) {
		if (object == null) {
			return null;
		}
		Class<?> clazz = object.getClass();
		try {
			Field field = clazz.getDeclaredField(fieldName);
			Method getter = getGetter(field);
			if (getter == null) {
				throw new IllegalArgumentException("getValue:"
						+ object.getClass().getName() + "." + fieldName
						+ " getter not found");
			}

			return getter.invoke(object);
		} catch (Exception e) {
			Log.e("ReflectUtil File", "getValue", e);
		}

		return null;
	}

	public static Object getValueOfField(Object object, String fieldName) {
		if (object == null) {
			return null;
		}
		Class<?> clazz = object.getClass();
		try {
			final Field field = clazz.getDeclaredField(fieldName);
			Method getter = getGetter(field);

			if (getter != null) {
				return getter.invoke(object);
			}

			field.setAccessible(true);
			return field.get(object);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}

	// public static Object invokeMethodNoParams(Class<?> mClass, Object obj,
	// String methodName) {
	// Method method = null;
	// Object result = null;
	// try {
	// method = mClass.getMethod(methodName, new Class[0]);
	// result = method.invoke(obj, new Object[0]);
	// } catch (Exception e) {
	// Log.e("ReflectUtil File",
	// "Exception in invokeMethod:" + methodName, e);
	// }
	//
	// return result;
	// }

	// public static Object invokeMethodWithParams(Class<?> mClass, Object obj,
	// String methodName, Object[] args, Class<?>[] types) {
	// Method method = null;
	// Object result = null;
	// try {
	// method = mClass.getMethod(methodName, types);
	// result = method.invoke(obj, args);
	// } catch (Exception e) {
	// Log.e("ReflectUtil File",
	// "Exception in invokeMethod:" + methodName, e);
	// }
	//
	// return result;
	// }

	public static boolean isBooleanType(Class<?> type) {
		return (type == Boolean.class) || (type == Boolean.TYPE);
	}

	public static boolean isByteType(Class<?> type) {
		return (type == Byte.class) || (type == Byte.TYPE);
	}

	public static boolean isCharType(Class<?> type) {
		return (type == Character.class) || (type == Character.TYPE);
	}

	public static boolean isDoubleType(Class<?> type) {
		return (type == Double.class) || (type == Double.TYPE);
	}

	public static boolean isFinal(Field field) {
		return Modifier.isFinal(field.getModifiers());
	}

	public static boolean isFloatType(Class<?> type) {
		return (type == Float.class) || (type == Float.TYPE);
	}

	public static boolean isIntegerType(Class<?> type) {
		return (type == Integer.class) || (type == Integer.TYPE);
	}

	public static boolean isLongType(Class<?> type) {
		return (type == Long.class) || (type == Long.TYPE);
	}

	public static boolean isPrimitiveType(Class<?> type) {
		if (type.isPrimitive()) {
			return true;
		}
		if (type == Boolean.class) {
			return true;
		}
		if (type == Character.class) {
			return true;
		}
		if (type == Byte.class) {
			return true;
		}
		if (type == Short.class) {
			return true;
		}
		if (type == Integer.class) {
			return true;
		}
		if (type == Long.class) {
			return true;
		}
		if (type == Float.class) {
			return true;
		}

		return type == Double.class;
	}

	public static boolean isShortType(Class<?> type) {
		return type == Short.class;
	}

	public static boolean isStatic(Field field) {
		return Modifier.isStatic(field.getModifiers());
	}

	public static void setField(Class<?> fClass, Object obj, String fieldName,
			Object fieldValue) {
		Field field = null;
		try {
			field = fClass.getField(fieldName);
			field.set(obj, fieldValue);
		} catch (Exception e) {
			Log.e("ReflectUtil File", "Exception in setField:" + fieldName, e);
		}
	}

	public static void setFieldValue(Object object, String fieldName,
			Object value) {
		final Field field = getDeclaredField(object, fieldName);
		try {
			field.setAccessible(true);
			field.set(object, value);
		} catch (IllegalArgumentException e) {
			Log.e("ReflectUtil File", e.getMessage(), e);
		} catch (IllegalAccessException e) {
			Log.e("ReflectUtil File", e.getMessage(), e);
		}
	}

	public static void setValue(Object object, String fieldName, Object value) {
		if (object == null) {
			return;
		}
		Class<?> clazz = object.getClass();
		try {
			Field field = clazz.getDeclaredField(fieldName);
			Method setter = getSetter(field);
			setter.invoke(object, value);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static boolean isBasicType(Class<?> clazz) {
		return (clazz == Integer.TYPE) || (clazz == Long.TYPE)
				|| (clazz == Float.TYPE) || (clazz == Double.TYPE)
				|| (clazz == Character.TYPE) || (clazz == Byte.TYPE)
				|| (clazz == Short.TYPE);
	}

	public static Object getBasicTypeNullValue(Class<?> clazz) {
		if (clazz == Integer.TYPE) {
			return Integer.valueOf(0);
		}
		if (clazz == Long.TYPE) {
			return Long.valueOf(0L);
		}
		if (clazz == Float.TYPE) {
			return Float.valueOf(0.0F);
		}
		if (clazz == Double.TYPE) {
			return Double.valueOf(0.0D);
		}
		if (clazz == Byte.TYPE) {
			return Byte.valueOf((byte) 0);
		}
		if (clazz == Short.TYPE) {
			return Short.valueOf((short) 0);
		}
		if (clazz == Character.TYPE) {
			return 0;
		}
		if (clazz == Boolean.TYPE) {
			return Boolean.FALSE;
		}

		return null;
	}

	public static void setValueOfField(Object object, String fieldName,
			Object value) {
		if (object == null) {
			return;
		}
		Class<?> clazz = object.getClass();
		try {
			final Field field = clazz.getDeclaredField(fieldName);
			Method setter = getSetter(field);
			if (setter != null) {
				Class<?> paramType = setter.getParameterTypes()[0];

				if (value == null) {
					value = getBasicTypeNullValue(paramType);
				}

				setter.invoke(object, value);
				return;
			}

			field.setAccessible(true);
			if (value == null) {
				value = getBasicTypeNullValue(object.getClass());
			}

			field.set(object, value);
		} catch (NoSuchFieldException e) {
			Log.e("ReflectUtil File", "setValueOfField:"
					+ object.getClass().getName() + "#" + fieldName);
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
			Log.e("ReflectUtil File", "setValueOfField:"
					+ object.getClass().getName() + "#" + fieldName);
			throw new IllegalArgumentException(e);
		} catch (InvocationTargetException e) {
			Log.e("ReflectUtil File", "setValueOfField:"
					+ object.getClass().getName() + "#" + fieldName);
			throw new IllegalArgumentException(e);
		} catch (IllegalArgumentException e) {
			Log.e("ReflectUtil File", "setValueOfField:"
					+ object.getClass().getName() + "#" + fieldName + "|"
					+ value);
			throw new IllegalArgumentException(e);
		}
	}
}
