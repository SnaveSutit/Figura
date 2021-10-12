package net.blancworks.figura.lua.api.model;

import net.blancworks.figura.PlayerData;
import net.blancworks.figura.lua.CustomScript;
import net.blancworks.figura.lua.api.ReadOnlyLuaTable;
import net.blancworks.figura.lua.api.ScriptLocalAPITable;
import net.blancworks.figura.lua.api.math.LuaVector;
import net.blancworks.figura.models.CustomModel;
import net.blancworks.figura.models.CustomModelPart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class CustomModelAPI {

    public static Identifier getID() {
        return new Identifier("default", "model");
    }

    public static ReadOnlyLuaTable getForScript(CustomScript script) {
        return new ScriptLocalAPITable(script, new LuaTable() {{
            if (script.playerData.model != null) {
                for (CustomModelPart part : script.playerData.model.allParts) {
                    set(part.name, new CustomModelPartTable(part, script.playerData));
                }
            }
        }});
    }

    private static class CustomModelPartTable extends ReadOnlyLuaTable {
        CustomModelPart targetPart;
        PlayerData partOwner;

        public CustomModelPartTable(CustomModelPart part, PlayerData owner) {
            super();
            targetPart = part;
            partOwner = owner;
            super.setTable(getTable());
        }

        public LuaTable getTable() {
            LuaTable ret = new LuaTable();

            int index = 1;
            for (CustomModelPart child : targetPart.children) {
                CustomModelPartTable tbl = new CustomModelPartTable(child, partOwner);
                ret.set(child.name, tbl);
                ret.set(index++, tbl);
            }

            ret.set("getPos", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaVector.of(targetPart.pos);
                }
            });

            ret.set("setPos", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    targetPart.pos = LuaVector.checkOrNew(arg1).asV3f();
                    return NIL;
                }
            });

            ret.set("getPivot", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaVector.of(targetPart.pivot);
                }
            });

            ret.set("setPivot", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    targetPart.pivot = LuaVector.checkOrNew(arg1).asV3f();
                    return NIL;
                }
            });

            ret.set("getColor", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaVector.of(targetPart.color);
                }
            });

            ret.set("setColor", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    targetPart.color = LuaVector.checkOrNew(arg1).asV3f();
                    return NIL;
                }
            });

            ret.set("getScale", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaVector.of(targetPart.scale);
                }
            });

            ret.set("setScale", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    targetPart.scale = LuaVector.checkOrNew(arg1).asV3f();
                    return NIL;
                }
            });

            ret.set("getRot", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaVector.of(targetPart.rot);
                }
            });

            ret.set("setRot", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    targetPart.rot = LuaVector.checkOrNew(arg1).asV3f();
                    return NIL;
                }
            });

            ret.set("getRebuiltUV", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    try {
                        CustomModelPart.UV uv = CustomModelPart.UV.valueOf(arg1.checkjstring());

                        if (uv == CustomModelPart.UV.ALL)
                            throw new LuaError("Cannot get UV data for ALL faces at once");

                        CustomModelPart.uvData data = targetPart.UVCustomizations.get(uv);
                        Vec2f offset = data.uvOffset;
                        Vec2f size = data.uvSize;

                        if (offset == null)
                            offset = new Vec2f(0f, 0f);

                        if (size == null)
                            size = new Vec2f(0f, 0f);

                        return new LuaVector(offset.x, offset.y, size.x - offset.x, size.y - offset.y);
                    } catch (Exception ignored) {
                        throw new LuaError("UV Type not found!");
                    }
                }
            });

            ret.set("rebuildUV", new TwoArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1, LuaValue arg2) {
                    try {
                        CustomModelPart.UV uv = CustomModelPart.UV.valueOf(arg1.checkjstring());

                        LuaVector vec = LuaVector.checkOrNew(arg2);
                        Vec2f offset = new Vec2f(vec.x(), vec.y());
                        Vec2f size = new Vec2f(vec.z() + vec.x(), vec.w() + vec.y());

                        if (uv == CustomModelPart.UV.ALL) {
                            targetPart.UVCustomizations.forEach((key, value) -> {
                                value.setUVOffset(offset);
                                value.setUVSize(size);
                            });
                        } else {
                            CustomModelPart.uvData data = targetPart.UVCustomizations.get(uv);
                            data.setUVOffset(offset);
                            data.setUVSize(size);
                        }

                        targetPart.applyUVMods(null);
                    } catch (Exception ignored) {
                        throw new LuaError("UV Type not found!");
                    }
                    return NIL;
                }
            });

            ret.set("getTextureSize", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    Vec2f uv = targetPart.texSize;
                    return LuaVector.of(uv);
                }
            });

            ret.set("setTextureSize", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    targetPart.applyUVMods(LuaVector.checkOrNew(arg));
                    return NIL;
                }
            });

            ret.set("getUV", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    Vec2f uv = targetPart.uvOffset;
                    return LuaVector.of(uv);
                }
            });

            ret.set("setUV", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    LuaVector vec = LuaVector.checkOrNew(arg);
                    targetPart.uvOffset = new Vec2f(vec.x(), vec.y());
                    return NIL;
                }
            });

            ret.set("getParentType", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaString.valueOf(targetPart.parentType.toString());
                }
            });

            ret.set("setParentType", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    CustomModel model = partOwner.model;
                    
                    if (targetPart.isParentSpecial()) {
                        model.worldParts.remove(targetPart);
                        model.leftElytraParts.remove(targetPart);
                        model.rightElytraParts.remove(targetPart);
                        model.skullParts.remove(targetPart);
                    }

                    try {
                        targetPart.parentType = CustomModelPart.ParentType.valueOf(arg1.checkjstring());

                        if (targetPart.isParentSpecial()) {
                            switch (targetPart.parentType) {
                                case WORLD -> model.worldParts.add(targetPart);
                                case LeftElytra -> model.leftElytraParts.add(targetPart);
                                case RightElytra -> model.rightElytraParts.add(targetPart);
                                case Skull -> model.skullParts.add(targetPart);
                            }
                        }
                    } catch (Exception ignored) {
                        targetPart.parentType = CustomModelPart.ParentType.Model;
                    }

                    return NIL;
                }
            });

            ret.set("getMimicMode", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaBoolean.valueOf(targetPart.isMimicMode);
                }
            });

            ret.set("setMimicMode", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    targetPart.isMimicMode = arg1.checkboolean();
                    return NIL;
                }
            });

            ret.set("getEnabled", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaBoolean.valueOf(targetPart.visible);
                }
            });

            ret.set("setEnabled", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    targetPart.visible = arg.checkboolean();
                    return null;
                }
            });

            ret.set("getHidden", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaBoolean.valueOf(targetPart.isHidden);
                }
            });

            ret.set("getShader", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaString.valueOf(targetPart.shaderType.toString());
                }
            });

            ret.set("setShader", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    try {
                        targetPart.shaderType = CustomModelPart.ShaderType.valueOf(arg1.checkjstring());
                    } catch (Exception ignored) {
                        targetPart.shaderType = CustomModelPart.ShaderType.None;
                    }

                    return NIL;
                }
            });

            ret.set("getTexture", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaString.valueOf(targetPart.textureType.toString());
                }
            });

            ret.set("setTexture", new TwoArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1, LuaValue arg2) {
                    try {
                        targetPart.textureType = CustomModelPart.TextureType.valueOf(arg1.checkjstring());

                        if (targetPart.textureType == CustomModelPart.TextureType.Resource)
                            targetPart.textureVanilla = new Identifier(arg2.checkjstring());
                    } catch (Exception ignored) {
                        targetPart.textureType = CustomModelPart.TextureType.Custom;
                    }

                    return NIL;
                }
            });

            ret.set("getExtraTexEnabled", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaBoolean.valueOf(targetPart.extraTex);
                }
            });

            ret.set("setExtraTexEnabled", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    targetPart.extraTex = arg.checkboolean();
                    return null;
                }
            });

            ret.set("partToWorldPos", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    LuaVector v = LuaVector.checkOrNew(arg1);

                    Vector4f v4f = new Vector4f(v.x() / 16f, v.y() / -16f, v.z() / 16f, 1f);

                    v4f.transform(targetPart.lastModelMatrix);

                    return LuaVector.of(new Vec3f(v4f.getX(), v4f.getY(), v4f.getZ()));
                }
            });

            ret.set("partToWorldDir", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    LuaVector v = LuaVector.checkOrNew(arg1);

                    Vec3f v3f = new Vec3f(v.x(), -v.y(), v.z());

                    v3f.transform(targetPart.lastNormalMatrix);

                    return LuaVector.of(v3f);
                }
            });

            ret.set("worldToPartPos", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    LuaVector v = LuaVector.checkOrNew(arg1);

                    Vector4f v4f = new Vector4f(v.x(), v.y(), v.z(), 1f);

                    v4f.transform(targetPart.lastModelMatrixInverse);

                    return LuaVector.of(new Vec3f(v4f.getX() * 16f, v4f.getY() * -16f, v4f.getZ() * 16f));
                }
            });

            ret.set("worldToPartDir", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    LuaVector v = LuaVector.checkOrNew(arg1);

                    Vec3f v3f = new Vec3f(v.x(), v.y(), v.z());

                    v3f.transform(targetPart.lastNormalMatrixInverse);

                    return LuaVector.of(new Vec3f(v3f.getX(), -v3f.getY(), v3f.getZ()));
                }
            });

            ret.set("getOpacity", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaNumber.valueOf(targetPart.alpha);
                }
            });

            ret.set("setOpacity", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    targetPart.alpha = Math.max(0f, Math.min(arg1.checknumber().tofloat(), 1f));
                    return NIL;
                }
            });

            ret.set("getType", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(targetPart.getPartType().toString());
                }
            });

            ret.set("getName", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(targetPart.name);
                }
            });

            ret.set("part", LuaValue.userdataOf(targetPart));

            return ret;
        }
    }

    public static CustomModelPart checkCustomModelPart(LuaValue arg1) {
        CustomModelPart part = (CustomModelPart) arg1.get("part").touserdata(CustomModelPart.class);
        if (part == null)
            throw new LuaError("Not a CustomModelPart table!");

        return part;
    }
}
