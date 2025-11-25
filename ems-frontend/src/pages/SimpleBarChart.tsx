// Modifică ems-frontend/src/pages/SimpleBarChart.tsx

import React, { useMemo } from 'react';

type ChartData = {
    label: string;
    value: number;
};

type SimpleBarChartProps = {
    data: ChartData[];
};

// --- Stilurile CSS ---
const chartStyles: React.CSSProperties = {
    display: 'flex',
    flexDirection: 'row',
    // alignItems: 'flex-end',  <-- SCOATEM ASTA de aici, o gestionăm în wrapper
    height: '250px',
    width: '100%',
    border: '1px solid #555',
    borderRadius: '4px',
    padding: '10px 5px 0 5px',
    boxSizing: 'border-box',
    gap: '0.5%',
};

const barWrapperStyle: React.CSSProperties = {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'flex-end', // Asta împinge bara jos
    height: '100%' // <--- ASTA LIPSEA! Acum wrapperul ocupă toata înălțimea graficului
};

const barStyle: React.CSSProperties = {
    width: '90%',
    backgroundColor: '#007bff',
    borderTopLeftRadius: '3px',
    borderTopRightRadius: '3px',
    transition: 'height 0.3s ease-out',
    position: 'relative',
    minHeight: '1px' // Opțional: ca să se vadă o linie fină chiar dacă e 0
};

const labelStyle: React.CSSProperties = {
    fontSize: '10px',
    marginTop: '4px',
    textAlign: 'center',
};

const valueStyle: React.CSSProperties = {
    position: 'absolute',
    top: '-20px',
    width: '100%',
    textAlign: 'center',
    fontSize: '11px',
    fontWeight: 'bold',
    color: 'white', // Dacă fundalul e închis, pune 'white', altfel 'black'
};
// --- Sfârșitul stilurilor ---


export const SimpleBarChart: React.FC<SimpleBarChartProps> = ({ data }) => {

    const maxValue = useMemo(() => {
        if (data.length === 0) return 1;
        return Math.max(...data.map(d => d.value));
    }, [data]);

    return (
        <div style={chartStyles}>
            {data.map((item, index) => {
                // Calculăm înălțimea
                const percentage = maxValue === 0 ? 0 : (item.value / maxValue) * 100;
                const barHeight = `${percentage}%`;

                return (
                    <div key={index} style={barWrapperStyle}>
                        <div style={{ ...barStyle, height: barHeight }}>
                            {/* Afișăm valoarea doar dacă e mai mare de 0 pentru a nu aglomera */}
                            {item.value > 0 && (
                                <span style={valueStyle}>{item.value.toFixed(2)}</span>
                            )}
                        </div>
                        <span style={labelStyle}>{item.label}</span>
                    </div>
                );
            })}
        </div>
    );
};