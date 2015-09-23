package org.spongepowered.common.data.manipulator.mutable.entity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHorseData;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseStyles;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.api.data.type.HorseVariants;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeHorseData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;

public class SpongeHorseData extends AbstractData<HorseData, ImmutableHorseData> implements HorseData {

    private HorseColor horseColor;
    private HorseStyle horseStyle;
    private HorseVariant horseVariant;

    public SpongeHorseData(HorseColor horseColor, HorseStyle horseStyle, HorseVariant horseVariant) {
        super(HorseData.class);
        this.horseColor = horseColor;
        this.horseStyle = horseStyle;
        this.horseVariant = horseVariant;
        registerGettersAndSetters();
    }

    public SpongeHorseData() {
        this(HorseColors.WHITE, HorseStyles.NONE, HorseVariants.HORSE);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.HORSE_COLOR, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getHorseColor();
            }
        });
        registerFieldSetter(Keys.HORSE_COLOR, new SetterFunction<Object>() {

            @Override
            public void set(Object value) {
                setHorseColor((HorseColor) value);
            }
        });
        registerKeyValue(Keys.HORSE_COLOR, new GetterFunction<Value<?>>() {

            @Override
            public Value<?> get() {
                return color();
            }
        });

        registerFieldGetter(Keys.HORSE_STYLE, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getHorseStyle();
            }
        });
        registerFieldSetter(Keys.HORSE_STYLE, new SetterFunction<Object>() {

            @Override
            public void set(Object value) {
                setHorseStyle((HorseStyle) value);
            }
        });
        registerKeyValue(Keys.HORSE_STYLE, new GetterFunction<Value<?>>() {

            @Override
            public Value<?> get() {
                return style();
            }
        });

        registerFieldGetter(Keys.HORSE_VARIANT, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getHorseVariant();
            }
        });
        registerFieldSetter(Keys.HORSE_VARIANT, new SetterFunction<Object>() {

            @Override
            public void set(Object value) {
                setHorseVariant((HorseVariant) value);
            }
        });
        registerKeyValue(Keys.HORSE_VARIANT, new GetterFunction<Value<?>>() {

            @Override
            public Value<?> get() {
                return variant();
            }
        });
    }

    @Override
    public Value<HorseColor> color() {
        return new SpongeValue<HorseColor>(Keys.HORSE_COLOR, HorseColors.WHITE, this.horseColor);
    }

    @Override
    public Value<HorseStyle> style() {
        return new SpongeValue<HorseStyle>(Keys.HORSE_STYLE, HorseStyles.NONE, this.horseStyle);
    }

    @Override
    public Value<HorseVariant> variant() {
        return new SpongeValue<HorseVariant>(Keys.HORSE_VARIANT, HorseVariants.HORSE, this.horseVariant);
    }

    @Override
    public HorseData copy() {
        return new SpongeHorseData(this.horseColor, this.horseStyle, this.horseVariant);
    }

    @Override
    public ImmutableHorseData asImmutable() {
        return new ImmutableSpongeHorseData(this.horseColor, this.horseStyle, this.horseVariant);
    }

    @Override
    public int compareTo(HorseData other) {
        return ComparisonChain.start()
                .compare(this.horseColor.getId(), other.color().get().getId())
                .compare(this.horseStyle.getId(), other.style().get().getId())
                .compare(this.horseVariant.getId(), other.variant().get().getId())
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.HORSE_COLOR.getQuery(), this.horseColor)
                .set(Keys.HORSE_STYLE.getQuery(), this.horseStyle)
                .set(Keys.HORSE_VARIANT.getQuery(), this.horseVariant);

    }

    private HorseColor getHorseColor() {
        return horseColor;
    }

    private void setHorseColor(HorseColor horseColor) {
        this.horseColor = horseColor;
    }

    private HorseStyle getHorseStyle() {
        return horseStyle;
    }

    private void setHorseStyle(HorseStyle horseStyle) {
        this.horseStyle = horseStyle;
    }

    private HorseVariant getHorseVariant() {
        return horseVariant;
    }

    private void setHorseVariant(HorseVariant horseVariant) {
        this.horseVariant = horseVariant;
    }
}
