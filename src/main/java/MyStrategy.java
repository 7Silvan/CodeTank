import model.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;
import java.util.List;

/**
 * Текущие идеи:
 * 1 подсчет статистики
 * 2 поворот башни если противник вне диапазона минимального поворота
 * 3 доезд до бонуса если нечем стрелять
 * 4 создание отсортированного списка отдаленности танков по углу
 * 5 создание отсортированного списка отдаленности танков по расстоянию
 * 6 не стрелять в мертвых
 * 7 по возможности повернуться ромбиком
 * 8 движение по окружности вокруг противника
 */
public final class MyStrategy implements Strategy {

    private final double MIN_ANGLE = Math.PI / 180.0; // угол в один градус;

    double leftTrackPower = 1.0D;
    double rightTrackPower = 1.0D;
    
    Tank self;
    World world;
    Move move;

    @Override
    public void move(Tank self, World world, Move move) {
        this.self = self;
        this.world = world;
        this.move = move;
        move();
    }

    @Override
    public TankType selectTank(int tankIndex, int teamSize) {
        return TankType.MEDIUM;
        //return TankType.TANK_DESTROYER;
        //return TankType.HEAVY;
    }

    private void move() {
        if (0 == self.getRemainingReloadingTime())
            killingTanks(self, world, move);
        else
            collectingBonuses(self, world, move);
    }

    /**
     * стратегия поиска и уничтожения танков
     *
     * @param self
     * @param world
     * @param move
     */
    private void killingTanks(Tank self, World world, Move move) {
        List<Tank> all_tanks = Arrays.asList(world.getTanks());                     // получим список всех танков
        Tank selected_tank = getNearerTank(all_tanks, null);
        
        
        if (selected_tank != null) {
            double angle_to_enemy = self.getTurretAngleTo(selected_tank); // найдем угол от башни до танка
            move.setFireType(FireType.NONE);
            if (angle_to_enemy > MIN_ANGLE) {         // если угол сильно положительный
                move.setTurretTurn(1.0);
            } else if (angle_to_enemy < -MIN_ANGLE) {  // если угол сильно отрицательный
                move.setTurretTurn(-1.0);
            } else {
                move.setFireType(FireType.PREMIUM_PREFERRED); // если угол невелик, то выстрелим
            }
        } else {
            log("[Killing mode] Selected tank is NULL");
        }
    }
    
    private Tank getFirstNotTeammateTank(List<Tank> all_tanks) {
        for (Tank tank : all_tanks)
            if (!tank.isTeammate())
                return tank;
        return null;
    }
    private void collectingBonuses(Tank self, World world, Move move) {
        List<Bonus> all_bonuses = Arrays.asList(world.getBonuses());             // получим список всех бонусов
        double min_dist_to_bonus = 1E20;
        Bonus selected_bonus = null;
        for (Bonus bonus : all_bonuses) {         // перебираем бонус из списка
            double dist_to_bonus = self.getDistanceTo(bonus);    // найдем расстояние до бонуса
            if (dist_to_bonus < min_dist_to_bonus) {             // найдем ближайший
                min_dist_to_bonus = dist_to_bonus;
                selected_bonus = bonus;
            }
        }

        if (selected_bonus != null) {
            double angle_to_bonus = self.getAngleTo(selected_bonus); // найдем угол до бонуса

            // TODO сделать более мягкий поворот, более точный
            if (angle_to_bonus > MIN_ANGLE) {         // если угол сильно положительный,
                move.setLeftTrackPower(1.0);      // то будем разворачиваться,
                move.setRightTrackPower(-1.0);        // поставив противоположные силы гусеницам.
            } else if (angle_to_bonus < -MIN_ANGLE) {  // если угол сильно отрицательный,
                move.setLeftTrackPower(-1.0);         // будем разворачиваться
                move.setRightTrackPower(1.0);     // в противоположную сторону.
            } else {
                move.setLeftTrackPower(1.0D);         // если угол не больше 30 градусов
                move.setRightTrackPower(1.0D);        // поедем максимально быстро вперед
            }
        }
    }


    // вспомагательные методы

    /**
     * Возвращает ближайший бонус указанного типа
     *
     * @param type  тип бонуса для поиска, если налл, то пофик какой
     * @return
     */
    private Bonus getNearerBonus(BonusType type) {
        //if (type == null) // если неважно какой
        // TODO implement
        throw new NotImplementedException();
    }

    /**
     * Возвращает ближайший танк указанного типа
     *
     * @param all_tanks
     * @param type  тип танка для поиска, если налл, то пофик какой
     * @return
     */
    private Tank getNearerTank(List<Tank> all_tanks, TankType type) {
        if (type == null) {// если неважно какой
            double min_angle_to_enemy = 1E20;
            Tank selected_tank = getFirstNotTeammateTank(all_tanks);  // в топку метод, под нож (нужен оптимальный выбор)
            for (Tank tank : all_tanks) {                               // перебираем танк из списка
                if (!tank.isTeammate()) {                               // в свои танки стрелять не будем :)
                    double angle_to_enemy = Math.abs(self.getTurretAngleTo(tank)); // найдем модуль угла от башни до танка
                    if (angle_to_enemy < min_angle_to_enemy) {          // выберем минимум
                        min_angle_to_enemy = angle_to_enemy;
                        selected_tank = tank;
                    }
                }
            }
            return  selected_tank;
        }
        throw new NotImplementedException();
    }

    private void log(String message) {
        System.out.println(message);
    }
}
